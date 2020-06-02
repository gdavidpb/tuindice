package com.gdavidpb.tuindice

const val CERT_HEADER = "-----BEGIN CERTIFICATE-----"
const val CERT_FOOTER = "-----END CERTIFICATE-----"
const val CERT_SEPARATOR = "$CERT_FOOTER\n$CERT_HEADER\n"

const val CERT_COUNT = 3

const
val CERT_DATA = "-----BEGIN CERTIFICATE-----\n" +
        "MIIDRjCCAi4CAQowDQYJKoZIhvcNAQEFBQAwgcgxIjAgBgNVBAoTGVVuaXZlcnNp\n" +
        "ZGFkIFNpbW9uIEJvbGl2YXIxKzApBgNVBAsTIkRpcmVjY2lvbiBkZSBTZXJ2aWNp\n" +
        "b3MgVGVsZW1hdGljb3MxIDAeBgkqhkiG9w0BCQEWEXNvcy1jYWNlcnRAdXNiLnZl\n" +
        "MRAwDgYDVQQHEwdDYXJhY2FzMRAwDgYDVQQIEwdNaXJhbmRhMQswCQYDVQQGEwJW\n" +
        "RTEiMCAGA1UEAxMZVW5pdmVyc2lkYWQgU2ltb24gQm9saXZhcjAeFw0xOTA2MTMx\n" +
        "MDMxMjVaFw0yMTA2MTIxMDMxMjVaMIGMMQswCQYDVQQGEwJWRTEQMA4GA1UECBMH\n" +
        "TWlyYW5kYTEiMCAGA1UEChMZVW5pdmVyc2lkYWQgU2ltb24gQm9saXZhcjErMCkG\n" +
        "A1UECxMiRGlyZWNjaW9uIGRlIFNlcnZpY2lvcyBUZWxlbWF0aWNvczEaMBgGA1UE\n" +
        "AxMRc2VjdXJlLmRzdC51c2IudmUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGB\n" +
        "ALRXiYN0CEtlW+Fir04pm/lIhZx7gg/kI9+SxIm26iHnmjSnko5rEoUJCh7RaYNE\n" +
        "eP4iycxUiTdxnja8SFCAS0dMIQKHN2boPKTHqTFzBXjHOB4vVg7SBLodbzf1hOzN\n" +
        "y1SgOQs3LueZjeJqFeERJTXcWChqeL54OKEUW3I1jwjhAgMBAAEwDQYJKoZIhvcN\n" +
        "AQEFBQADggEBAJaLJapjfUxDYBUZSYuCXx1yn20clEPFt+1MFAJLy1SEBDkZ4F2F\n" +
        "E9Op+mnYx7H9+9xnBiCn2O972glFp4V5ou4SBmA/HFy4ZRJxgg7agn2bsQVvmPaL\n" +
        "br5xQcsJEkmfIbNw2z7647Jl3lcPay/groVpOVDzUdOL9f9DmWKztL2dxmLzK//7\n" +
        "/oXlMWk60hVsLMa2D9pddwnBGkHd2l5bv5+Jzel0P4XkkSj7YZCgwMZR5vcURU59\n" +
        "2gVp1ia7RqMfpH4zxIEKnWytTo59VFbCffUUZiV85g+JUtWLFTok7e5W5UvVvPO4\n" +
        "wyM3cgXJP399NDBF6r65Q2tU2P0IFowPkgc=\n" +
        "-----END CERTIFICATE-----\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIID+TCCAuGgAwIBAgIEYUD5fjANBgkqhkiG9w0BAQsFADCBrDELMAkGA1UEBhMC\n" +
        "VkUxEDAOBgNVBAgTB01pcmFuZGExDzANBgNVBAcTBkJhcnV0YTEyMDAGA1UECgwp\n" +
        "RGlyZWNjacOzbiBkZSBJbmdlbmllcsOtYSBkZSBJbmZvcm1hY2nDs24xJDAiBgNV\n" +
        "BAsMG1VuaXZlcnNpZGFkIFNpbcOzbiBCb2zDrXZhcjEgMB4GA1UEAxMXYXBsaWNh\n" +
        "Y2lvbmVzLmRpaS51c2IudmUwHhcNMTUwMTI3MTQ0NTM0WhcNMTgwMTI2MTQ0NTM0\n" +
        "WjCBrDELMAkGA1UEBhMCVkUxEDAOBgNVBAgTB01pcmFuZGExDzANBgNVBAcTBkJh\n" +
        "cnV0YTEyMDAGA1UECgwpRGlyZWNjacOzbiBkZSBJbmdlbmllcsOtYSBkZSBJbmZv\n" +
        "cm1hY2nDs24xJDAiBgNVBAsMG1VuaXZlcnNpZGFkIFNpbcOzbiBCb2zDrXZhcjEg\n" +
        "MB4GA1UEAxMXYXBsaWNhY2lvbmVzLmRpaS51c2IudmUwggEiMA0GCSqGSIb3DQEB\n" +
        "AQUAA4IBDwAwggEKAoIBAQCd4RQk5CNZxnc+bJ/xxRbLAYdJ/d61zy9AZO5VHKxj\n" +
        "67OuYL4WXVmzxNcJ2EVuY/gerZsseDhUYw6pB7ccbygZYirz/MwNvZtsbPP3a5Qe\n" +
        "+ERw9rYKso+CGQ8ESVmJn0iBmBWOQvqdCgVLwR0l7ZrOOv566RYp91ZhpQnhH5L+\n" +
        "N7zYz1C9VqdnN+uObyr5Yrt87wtysN4mKHXOQqDSYp6TTpQcxnhfdyv3ecqD07dN\n" +
        "+JF1i3Oaao0Ka3e4KTPvcV7qcAz/rtJ1fuxyGn6AvpiYq4k4RvA0mQoIIDyal171\n" +
        "E5sMy3iiAiAvfkceldvAhfggjeEcj9JfM6aLVLOmsEAjAgMBAAGjITAfMB0GA1Ud\n" +
        "DgQWBBSgmZejnefxlLgtWLJJT1GRAFs6GzANBgkqhkiG9w0BAQsFAAOCAQEAcF4I\n" +
        "6XNMyjvav5MLiRd6nQrf7YFGtsiw8oDE9IsUc+NA3pFKoQE0eS4I6wh+xxAHobhl\n" +
        "GEHUtvEheEO4r+7ab16EFpIt4Q3OsKCoxnmC51zb/k7J5myv2nAj/Dz7Ge0kr/MS\n" +
        "tAK04sJPNGssVa7X/+K+mZhq5Sj5vV2JcCDnKfDAMmeonxRdW765UhusMOkUbrFg\n" +
        "Mbl566a82QfmZnyK91549T8sVAARYHPsfgqK2UOkmErbbL1dI67VuCq4ALljpLR9\n" +
        "vGfFC+Sq2dkAQuZCCf1wa3W6EYMU7d8LQKLNR3OCr9D6XVc4HmTFkhbazfBmCllJ\n" +
        "1IOX6WBQ6IMMVhYTCw==\n" +
        "-----END CERTIFICATE-----\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIDAzCCAmwCCQDcrgmx+0UIQzANBgkqhkiG9w0BAQUFADCBxTELMAkGA1UEBhMC\n" +
        "VkUxEDAOBgNVBAgTB0NhcmFjYXMxDzANBgNVBAcTBkJhcnV0YTEiMCAGA1UEChQZ\n" +
        "VW5pdmVyc2lkYWQgU2lt824gQm9s7XZhcjEvMC0GA1UECxQmRGlyZWNjafNuIGRl\n" +
        "IEluZ2VuaWVy7WEgZGUgSW5mb3JtYWNp824xHzAdBgNVBAMTFmluc2NyaXBjaW9u\n" +
        "LmRpaS51c2IudmUxHTAbBgkqhkiG9w0BCQEWDnVzYi1kaWlAdXNiLnZlMB4XDTA5\n" +
        "MTIwNDEzMzEyNVoXDTE5MTIwMjEzMzEyNVowgcUxCzAJBgNVBAYTAlZFMRAwDgYD\n" +
        "VQQIEwdDYXJhY2FzMQ8wDQYDVQQHEwZCYXJ1dGExIjAgBgNVBAoUGVVuaXZlcnNp\n" +
        "ZGFkIFNpbfNuIEJvbO12YXIxLzAtBgNVBAsUJkRpcmVjY2nzbiBkZSBJbmdlbmll\n" +
        "cu1hIGRlIEluZm9ybWFjafNuMR8wHQYDVQQDExZpbnNjcmlwY2lvbi5kaWkudXNi\n" +
        "LnZlMR0wGwYJKoZIhvcNAQkBFg51c2ItZGlpQHVzYi52ZTCBnzANBgkqhkiG9w0B\n" +
        "AQEFAAOBjQAwgYkCgYEAwEapQUiuYK37p5qeF4Z09yHmL3jwUSUW6XudTdNRLUnJ\n" +
        "l6amYmYkW3kEoNWB3AXy9YSylVDbWNwGsU96lkOUhTIQDqZuUAMBmRg6Xyv3gIGb\n" +
        "U5TgGRtQpMXIKt2zRF0UTQwRkF1L/LYl4Z9PHJMur3d21N61XSwO9IwTCvwYRHkC\n" +
        "AwEAATANBgkqhkiG9w0BAQUFAAOBgQCmD9YGdFj4ajUDXBxyKpH4KhIAdGOQFBSI\n" +
        "EY+90R67nsr74eC+g0WffR+ySa8rwYKkMpTj7EzPEeNvpmbz1igKPCe7Ye/lPlYw\n" +
        "LbUmv9ZnbMAvL2PES9eIfttrginBjwD+yi5Vrte211LuUAiOiQpCg3CeCD5hGmMy\n" +
        "gGOe5rdEeg==\n" +
        "-----END CERTIFICATE-----"