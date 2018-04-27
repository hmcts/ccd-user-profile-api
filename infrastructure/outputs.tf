output "vaultUri" {
  value = "${module.user-profile-vault.key_vault_uri}"
}

output "vaultName" {
  value = "${module.user-profile-vault.key_vault_name}"
}

output "s2s_url" {
  value = "${var.s2s_url}"
}
