variable "product" {
}

variable "raw_product" {
  default = "ccd" // jenkins-library overrides product for PRs and adds e.g. pr-118-ccd
}

variable "component" {
}

variable "location" {
  default = "UK South"
}

variable "env" {
}

variable "subscription" {
}

////////////////////////////////
// Database
////////////////////////////////

variable "postgresql_user" {
  default = "ccd"
}

variable "database_name" {
  default = "ccd_user_profile"
}

variable "common_tags" {
  type = map(string)
}
variable "pgsql_sku" {
  description = "The PGSql flexible server instance sku"
  default     = "GP_Standard_D2s_v3"
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "aks_subscription_id" {}

variable "pgsql_storage_mb" {
  description = "Max storage allowed for the PGSql Flexibile instance"
  type        = number
  default     = 65536
}

variable "subnet_suffix" {
  default     = null
  type        = string
  description = "Suffix to append to the subnet name, the originally created one used by this module is full in a number of environments."
}

variable "action_group_name" {
  description = "The name of the Action Group to create."
  type        = string
  default     = "action_group"
}

variable "email_address_key" {
  description = "Email address key in azure Key Vault."
  type        = string
  default     = "db-alert-monitoring-email-address"
}

variable "cpu_threshold" {
  default     = 1
  type        = number
  description = "Average CPU utilisation threshold"
}

variable "memory_threshold" {
  default     = 5
  type        = number
  description = "Average memory utilisation threshold"
}

variable "storage_threshold" {
  default     = 0.5
  type        = number
  description = "Average storage utilisation threshold"
}
