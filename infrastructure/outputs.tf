output "user_profile_api_deployment_endpoint" {
  value = "${module.user-profile-api.gitendpoint}"
}

output "s2s_url" {
  value = "${var.s2s_url}"
}
