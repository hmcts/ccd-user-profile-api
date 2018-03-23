variable "product" {
  type    = "string"
  default = "ccd"
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

variable "ilbIp"{}

variable "s2s_url" {
  default = "http://betaDevBccidamS2SLB.reform.hmcts.net"
}

variable "authorised-services" {
  type    = "string"
  default = "ccd_data,ccd_definition"
}
