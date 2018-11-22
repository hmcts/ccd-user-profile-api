locals {
  app_full_name = "${var.product}-${var.component}"
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

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

  app_settings = {
<<<<<<< HEAD
<<<<<<< HEAD
    USER_PROFILE_DB_HOST        = "${module.user-profile-db.host_name}"
    USER_PROFILE_DB_PORT        = "${module.user-profile-db.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${module.user-profile-db.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${module.user-profile-db.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${module.user-profile-db.postgresql_password}"

    ENABLE_DB_MIGRATE = "false"

    IDAM_S2S_URL = "${local.s2s_url}"
=======
    USER_PROFILE_DB_HOST        = "${module.db-user-profile.host_name}"
    USER_PROFILE_DB_PORT        = "${module.db-user-profile.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${module.db-user-profile.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${module.db-user-profile.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${module.db-user-profile.postgresql_password}"
=======
    USER_PROFILE_DB_HOST        = "${var.use_uk_db != "true" ? module.postgres-user-profile.host_name : module.user-profile-db.host_name}"
    USER_PROFILE_DB_PORT        = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_listen_port : module.user-profile-db.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_database : module.user-profile-db.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${var.use_uk_db != "true" ? module.postgres-user-profile.user_name : module.user-profile-db.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_password : module.user-profile-db.postgresql_password}"
>>>>>>> 79499b8... Both DB with UK switch off
    IDAM_S2S_URL = "${var.s2s_url}"
>>>>>>> ee411bd... Try to destroy old DB, test lock
    USER_PROFILE_S2S_AUTHORISED_SERVICES = "${var.authorised-services}"
  }

}

<<<<<<< HEAD
<<<<<<< HEAD
module "user-profile-db" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
=======
//module "postgres-user-profile" {
//  source              = "git@github.com:contino/moj-module-postgres?ref=master"
//  product             = "${var.product}-user-profile"
//  location            = "West Europe"
//  env                 = "${var.env}"
//  postgresql_user     = "ccd"
//}
=======
module "postgres-user-profile" {
  source              = "git@github.com:contino/moj-module-postgres?ref=master"
  product             = "${var.product}-user-profile"
  location            = "West Europe"
  env                 = "${var.env}"
  postgresql_user     = "ccd"
}
>>>>>>> 79499b8... Both DB with UK switch off

module "user-profile-db" {
  source = "git@github.com:hmcts/moj-module-postgres?ref=cnp-449-tactical"
>>>>>>> ee411bd... Try to destroy old DB, test lock
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


////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${local.app_full_name}-POSTGRES-USER"
  value = "${module.user-profile-db.user_name}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_key_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${local.app_full_name}-POSTGRES-PASS"
  value = "${module.user-profile-db.postgresql_password}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_key_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${local.app_full_name}-POSTGRES-HOST"
  value = "${module.user-profile-db.host_name}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_key_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${local.app_full_name}-POSTGRES-PORT"
  value = "${module.user-profile-db.postgresql_listen_port}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_key_vault.vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${local.app_full_name}-POSTGRES-DATABASE"
  value = "${module.user-profile-db.postgresql_database}"
  vault_uri = "${data.azurerm_key_vault.ccd_shared_key_vault.vault_uri}"
}
