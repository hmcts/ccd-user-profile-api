provider "vault" {
  //  # It is strongly recommended to configure this provider through the
  //  # environment variables described above, so that each user can have
  //  # separate credentials set in the environment.
  //  #
  //  # This will default to using $VAULT_ADDR
  //  # But can be set explicitly
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "ccd_data_s2s_key" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/ccd-data"
}

locals {
  app_full_name = "${var.product}-${var.component}"
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  env_ase_url = "${local.local_env}.service.${local.local_ase}.internal"

  // Vault name
  previewVaultName = "${var.raw_product}-shared-aat"
  nonPreviewVaultName = "${var.raw_product}-shared-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  s2s_url = "http://rpe-service-auth-provider-${local.env_ase_url}"

  ### TODO Remove with old vault
  oldPreviewVaultName = "${var.product}-profile"
  oldNonPreviewVaultName = "${local.oldPreviewVaultName}-${var.env}"
  oldVaultName = "${(var.env == "preview" || var.env == "spreview") ? local.oldPreviewVaultName : local.oldNonPreviewVaultName}"
  oldPreviewVaultUri = "https://ccd-profile-aat.vault.azure.net/"
  oldNonPreviewVaultUri = "${module.user-profile-vault.key_vault_uri}"
  oldVaultUri = "${(var.env == "preview" || var.env == "spreview") ? local.oldPreviewVaultUri : local.oldNonPreviewVaultUri}"
}

data "azurerm_key_vault" "ccd_shared_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

module "user-profile-api" {
  source   = "git@github.com:contino/moj-module-webapp?ref=master"
  product  = "${local.app_full_name}"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"
  subscription = "${var.subscription}"
  is_frontend = false
  common_tags  = "${var.common_tags}"

  app_settings = {
    USER_PROFILE_DB_HOST        = "${module.user-profile-db.host_name}"
    USER_PROFILE_DB_PORT        = "${module.user-profile-db.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${module.user-profile-db.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${module.user-profile-db.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${module.user-profile-db.postgresql_password}"

    ENABLE_DB_MIGRATE = "false"

    IDAM_S2S_URL = "${local.s2s_url}"
    USER_PROFILE_S2S_AUTHORISED_SERVICES = "${var.authorised-services}"
  }

}

module "user-profile-db" {
  source = "git@github.com:hmcts/moj-module-postgres?ref=master"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
  common_tags  = "${var.common_tags}"
}

module "user-profile-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${local.oldVaultName}" // Max 24 characters
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.user-profile-api.resource_group_name}"
  product_group_object_id = "be8b3850-998a-4a66-8578-da268b8abd6b"
}


////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${local.app_full_name}-POSTGRES-USER"
  value = "${module.user-profile-db.user_name}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${local.app_full_name}-POSTGRES-PASS"
  value = "${module.user-profile-db.postgresql_password}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${local.app_full_name}-POSTGRES-HOST"
  value = "${module.user-profile-db.host_name}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${local.app_full_name}-POSTGRES-PORT"
  value = "${module.user-profile-db.postgresql_listen_port}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${local.app_full_name}-POSTGRES-DATABASE"
  value = "${module.user-profile-db.postgresql_database}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ds_s2s_key" {
  name = "ccd-data-s2s-key"
  value = "${data.vault_generic_secret.ccd_data_s2s_key.data["value"]}"
  vault_uri = "${module.user-profile-vault.key_vault_uri}"
}
