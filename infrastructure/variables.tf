variable "product" {
  type    = "string"
}

variable "raw_product" {
  default = "ccd" // jenkins-library overrides product for PRs and adds e.g. pr-118-ccd
}

variable "component" {
  type    = "string"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "subscription" {
  type = "string"
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
  type = "map"
}
