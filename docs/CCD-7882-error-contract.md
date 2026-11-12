# CCD-7882 error contract

Related requirements: CCD-7108 and CME-984.

Prevent public errors from revealing exception classes, stack traces, internal URLs, file paths
or software details. Preserve response schemas, HTTP status mappings, approved business messages
and public `message` fields where present, while retaining useful private diagnostics.

## Responses

- Disable automatic exception messages, binding errors and stack traces in fallback responses. Hide
  health details and native Tomcat reports/version information.
- Keep approved bad-request/not-found text and the corresponding `Message` header. Unexpected
  failures use a generic string response; SQL failures use `{ "errorMessage": "Internal Server
  Error" }` without SQL details.
- Framework errors retain `ProblemDetail` fields with generic detail and `instance: "/"`. Preserve
  400/404 application mappings and 500 unexpected/SQL mappings.
- Add no public error-code field.

## Diagnostics

`ErrorCode` labels are internal log categories; `failureCode` is the structured telemetry code.
Neither introduces a public API error-code field.

Handlers retain original exceptions at WARN for 4xx and ERROR for 5xx, with private `failureCode`
and `httpStatus` dimensions. Correlation context is preserved.

## Verification

Focused response and diagnostic tests cover status, body/header contracts and private exception
details. Docker checks cover native malformed requests, correlated failure telemetry and DEBUG
ingestion with application/agent DEBUG enabled.

## Support

In Application Insights, filter private `failureCode` and `httpStatus` dimensions, then use
`operation_Id` to find related requests and dependencies. Inspect exception details in restricted
telemetry or server logs; public error messages are sanitised. Failure diagnosis should work at
normal production logging levels without enabling DEBUG.

## Deployment checks

- Verify effective error/logging settings and sanitised responses in each environment. Confirm
  `server.error.include-message=never` at runtime.
- Confirm diagnostic logs and Application Insights are restricted to authorised personnel, with
  suitable sampling and retention.
- Verify failure codes and correlation IDs are searchable at normal production logging levels;
  run deployed consumer/Pact checks.
