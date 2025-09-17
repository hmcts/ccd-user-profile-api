# PG Flexible Server SKU
pgsql_sku     = "GP_Standard_D2s_v3"
subnet_suffix = "expanded"

variable "pgsql_storage_mb" {
  description = "Max storage allowed for the PGSql Flexibile instance"
  type        = number
  default     = 131072
}
