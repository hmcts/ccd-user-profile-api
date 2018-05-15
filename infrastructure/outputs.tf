output "microserviceName" {
  value = "${local.app_full_name}"
}

output "vaultUri" {
  value = "${local.vaultUri}"
}

output "vaultName" {
  value = "${local.vaultName}"
}
output "s2s_url" {
  value = "${local.s2s_url}"
}

output "CCD_DS_SERVICE_NAME" {
  value = "ccd_data"
}
