locals {
  app_full_name = "${var.product}-${var.component}"

  // Vault name
  previewVaultName = "ccd-profile-aat"
  nonPreviewVaultName = "ccd-profile-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  // Vault URI
  previewVaultUri = "https://${local.previewVaultName}.vault.azure.net/"
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
    USER_PROFILE_DB_HOST        = "${module.postgres-user-profile.host_name}"
    USER_PROFILE_DB_PORT        = "${module.postgres-user-profile.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${module.postgres-user-profile.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${module.postgres-user-profile.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${module.postgres-user-profile.postgresql_password}"
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
