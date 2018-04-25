locals {
  app_full_name = "${var.product}-${var.component}"
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
    USER_PROFILE_DB_HOST        = "${var.use_uk_db != "true" ? module.postgres-user-profile.host_name : module.db-user-profile.host_name}"
    USER_PROFILE_DB_PORT        = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_listen_port : module.db-user-profile.postgresql_listen_port}"
    USER_PROFILE_DB_NAME        = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_database : module.db-user-profile.postgresql_database}"
    USER_PROFILE_DB_USERNAME    = "${var.use_uk_db != "true" ? module.postgres-user-profile.user_name : module.db-user-profile.user_name}"
    USER_PROFILE_DB_PASSWORD    = "${var.use_uk_db != "true" ? module.postgres-user-profile.postgresql_password : module.db-user-profile.postgresql_password}"
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

module "db-user-profile" {
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
