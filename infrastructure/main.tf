locals {
  app_full_name = "${var.product}-${var.component}"

  // Vault name
  previewVaultName = "${var.product}-profile"
  # preview env contains pr number prefix, other envs need a suffix
  nonPreviewVaultName = "${local.previewVaultName}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  // Vault URI
  previewVaultUri = "https://ccd-profile-aat.vault.azure.net/"
  nonPreviewVaultUri = "${module.user-profile-vault.key_vault_uri}"
  vaultUri = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultUri : local.nonPreviewVaultUri}"
}

module "user-profile-api" {
  source   = "git@github.com:contino/moj-module-webapp?ref=master"
  product  = "${local.app_full_name}"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"
  subscription = "${var.subscription}"
  is_frontend = false

  app_settings = {
    USER_PROFILE_DB_HOST        = "${var.use_uk_db != "true" ? module.postgres-user-profile.host_name : module.user-profile-db.host_name}"
    USER_PROFILE_DB_PORT        = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_listen_port : module.user-profile-db.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_database : module.user-profile-db.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${var.use_uk_db != "true" ? module.postgres-user-profile.user_name : module.user-profile-db.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_password : module.user-profile-db.postgresql_password}"

    UK_DB_HOST = "${module.user-profile-db.host_name}"
    UK_DB_PORT = "${module.user-profile-db.postgresql_listen_port}"
    UK_DB_NAME = "${module.user-profile-db.postgresql_database}"
    UK_DB_USERNAME = "${module.user-profile-db.user_name}"
    UK_DB_PASSWORD = "${module.user-profile-db.postgresql_password}"

    IDAM_S2S_URL = "${var.s2s_url}"
    USER_PROFILE_S2S_AUTHORISED_SERVICES = "${var.authorised-services}"
  }

}

module "postgres-user-profile" {
  source              = "git@github.com:contino/moj-module-postgres?ref=master"
  product             = "${var.product}-user-profile"
  location            = "West Europe"
  env                 = "${var.env}"
  postgresql_user     = "ccd"
}

module "user-profile-db" {
  source = "git@github.com:hmcts/moj-module-postgres?ref=cnp-449-tactical"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
}

module "user-profile-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${local.vaultName}" // Max 24 characters
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.user-profile-api.resource_group_name}"
  product_group_object_id = "be8b3850-998a-4a66-8578-da268b8abd6b"
}
