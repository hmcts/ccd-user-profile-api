provider "azurerm" {
  version = "1.22.1"
}

locals {
  app_full_name = "${var.product}-${var.component}"
  aseName = "core-compute-${var.env}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  env_ase_url = "${local.local_env}.service.${local.local_ase}.internal"

  // Vault name
  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  // Shared Resource Group
  previewResourceGroup = "${var.raw_product}-shared-aat"
  nonPreviewResourceGroup = "${var.raw_product}-shared-${var.env}"
  sharedResourceGroup = "${(var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup}"

  sharedAppServicePlan = "${var.raw_product}-${var.env}"
  sharedASPResourceGroup = "${var.raw_product}-shared-${var.env}"

  s2s_url = "http://rpe-service-auth-provider-${local.env_ase_url}"
}

data "azurerm_key_vault" "ccd_shared_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

module "user-profile-api" {
  source   = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product  = "${local.app_full_name}"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"
  subscription = "${var.subscription}"
  is_frontend = false
  common_tags  = "${var.common_tags}"
  asp_name = "${(var.asp_name == "use_shared") ? local.sharedAppServicePlan : var.asp_name}"
  asp_rg = "${(var.asp_rg == "use_shared") ? local.sharedASPResourceGroup : var.asp_rg}"
  website_local_cache_sizeinmb = 1000
  capacity = "${var.capacity}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  java_container_version = "9.0"

  app_settings = {
    USER_PROFILE_DB_HOST        = "${module.user-profile-db.host_name}"
    USER_PROFILE_DB_PORT        = "${module.user-profile-db.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${module.user-profile-db.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${module.user-profile-db.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${module.user-profile-db.postgresql_password}"

    USER_PROFILE_DB_OPTIONS     = "?sslmode=require"

    ENABLE_DB_MIGRATE = "false"

    IDAM_S2S_URL = "${local.s2s_url}"
    USER_PROFILE_S2S_AUTHORISED_SERVICES = "${var.authorised-services}"
  }

}

module "user-profile-db" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  subscription = "${var.subscription}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
  common_tags  = "${var.common_tags}"
}


////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${var.component}-POSTGRES-USER"
  value = "${module.user-profile-db.user_name}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${var.component}-POSTGRES-PASS"
  value = "${module.user-profile-db.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${var.component}-POSTGRES-HOST"
  value = "${module.user-profile-db.host_name}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${var.component}-POSTGRES-PORT"
  value = "${module.user-profile-db.postgresql_listen_port}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${var.component}-POSTGRES-DATABASE"
  value = "${module.user-profile-db.postgresql_database}"
  key_vault_id = "${data.azurerm_key_vault.ccd_shared_key_vault.id}"
}
