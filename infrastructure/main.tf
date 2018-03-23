module "user-profile-api" {
  source   = "git@github.com:contino/moj-module-webapp?ref=master"
  product  = "${var.product}-user-profile-api"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"
  subscription = "${var.subscription}"

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
