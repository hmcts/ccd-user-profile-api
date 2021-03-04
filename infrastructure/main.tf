provider "azurerm" {
  features {}
}

locals {
  app_full_name = "${var.product}-${var.component}"

  // Vault name
  vaultName = "${var.product}-${var.env}"

  // Shared Resource Group
  sharedResourceGroup = "${var.product}-shared-${var.env}"

}

data "azurerm_key_vault" "ccd_shared_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.user-profile-db-v11.user_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.user-profile-db-v11.postgresql_password
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.user-profile-db-v11.host_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = module.user-profile-db-v11.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = module.user-profile-db-v11.postgresql_database
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}


////////////////////////////////
// DB version 11              //
////////////////////////////////

module "user-profile-db-v11" {
  source          = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product         = "${var.component}-db-v11"
  component       = var.component
  name            = "${local.app_full_name}-postgres-db-v11"
  location        = "${var.location}"
  env             = "${var.env}"
  subscription    = "${var.subscription}"
  postgresql_user = "${var.postgresql_user}"
  database_name   = "${var.database_name}"
  postgresql_version = "11"
  sku_name        = "GP_Gen5_2"
  sku_tier        = "GeneralPurpose"
  storage_mb      = "51200"
  common_tags     = "${var.common_tags}"
}
