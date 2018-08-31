output "microserviceName" {
  value = "${local.app_full_name}"
}

output "vaultUri" {
  value = "${local.oldVaultUri}"
}

output "vaultName" {
  value = "${local.oldVaultName}"
}
output "s2s_url" {
  value = "${local.s2s_url}"
}

output "CCD_DS_SERVICE_NAME" {
  value = "ccd_data"
}
