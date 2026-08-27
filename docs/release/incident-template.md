# Production Incident Template

Reviewed: 2026-08-27. Create one copy per suspected release incident. Keep this
record access-controlled and sanitized.

## Status Vocabulary

- `CODE-VERIFIED`: supported by immutable repository/artifact evidence.
- `PENDING`: awaiting investigation, external evidence, or owner decision.
- `BLOCKED`: release/promotion must not continue.
- `NOT APPLICABLE`: reviewed and inapplicable, with rationale.

## Incident Header

| Field | Value |
| --- | --- |
| Incident status | `PENDING` |
| Severity | `PENDING` |
| Incident commander | `PENDING` |
| Opened / detected (UTC) | `PENDING` / `PENDING` |
| Detection source | `PENDING` |
| Affected package/version code/version name | `PENDING` |
| Affected rollout stage/population | `PENDING` |
| User impact and regions/devices | `PENDING` |
| Data/privacy/security impact | `PENDING` |
| Communication owner/channel | `PENDING` |

## Artifact Identity

| Field | Value / evidence |
| --- | --- |
| Commit SHA / source ref | `PENDING` |
| AAB SHA-256 / artifact URL | `PENDING` |
| Signing certificate SHA-256 | `PENDING` |
| Version code / version name | `PENDING` |
| Mapping/provenance SHA-256 | `PENDING` |
| Crashlytics mapping receipt | `PENDING` or `NOT APPLICABLE` |

## Immediate Containment

- [ ] Set status `BLOCKED`; stop further promotion.
- [ ] Halt the staged release, or halt the fully rolled release when supported.
- [ ] Confirm the affected artifact identity before diagnosing or communicating.
- [ ] Disable or isolate an external service only through its approved owner and
      change procedure; record scope and user effect.
- [ ] Preserve evidence before log expiry, cleanup, dashboard changes, or a fix.
- [ ] Escalate any suspected data/privacy/security issue to the designated owner.

## Evidence Preservation

Preserve immutable AAB/provenance/checksums, mapping and upload receipt,
workflow/test URLs, store/vitals screenshots or exports with UTC timestamps,
redacted Crashlytics event identifiers and trends, affected device/build data,
support themes, network/Maps/consent health, timeline, decisions, and every
configuration change. Record evidence location, collector, collection time,
hash where practical, access restriction, and owner-approved retention period.

Do not place credentials, API keys, raw precise coordinates or addresses, ad
identifiers, consent payload values, complete support messages, email addresses,
or other unnecessary personal data in this record. Preserve sensitive source
evidence only in an approved restricted system and link a sanitized reference.
Do not delete or rotate incident data before the incident commander confirms
preservation; rotate a credential promptly if exposure is suspected while
retaining only non-secret audit evidence.

## Timeline

| Time (UTC) | Actor | Observation/action | Evidence | Decision/status |
| --- | --- | --- | --- | --- |
| `PENDING` | `PENDING` | `PENDING` | `PENDING` | `PENDING` |

## Triage And Scope

| Question | Finding / evidence |
| --- | --- |
| Reproducible on clean install, upgrade, or both? | `PENDING` |
| Affected OS/device/locale/network/geography? | `PENDING` |
| Started with this version or external configuration change? | `PENDING` |
| Maps, USGS, Geocoder, Ads/UMP, Firebase, or Play outage/configuration involved? | `PENDING` |
| Crash/ANR rates versus stable version and official threshold? | `PENDING` |
| Data loss, Room/DataStore migration, backup/deletion issue? | `PENDING` |
| Consent/privacy declaration mismatch? | `PENDING` |
| Root cause / contributing factors / confidence | `PENDING` |

## Recovery Decision

| Option | Decision / rationale / approver |
| --- | --- |
| Keep halted and investigate | `PENDING` |
| Resume unchanged artifact | `PENDING`; requires disproven impact and owner approval |
| External configuration correction | `PENDING`; re-run affected compliance and device gates |
| Higher versionCode hotfix | `PENDING`; use complete candidate pipeline and rollout record |

Do not replace, edit, or re-sign the accepted AAB in place. A code/resource fix
must use a higher versionCode, new immutable provenance, the protected signed
workflow, compliance re-review, internal testing, and a controlled rollout.

## Resolution And Follow-Up

| Field | Value |
| --- | --- |
| Containment verified | `PENDING` |
| User/service recovery verified | `PENDING` |
| Final impact | `PENDING` |
| Root cause | `PENDING` |
| Corrective commit/configuration | `PENDING` |
| Hotfix rollout record | `PENDING` or `NOT APPLICABLE` |
| Privacy/security notification decision | `PENDING` |
| Follow-up owners/dates | `PENDING` |
| Incident closed by/date | `PENDING` |

## Official Sources

- [Google Play staged rollouts](https://support.google.com/googleplay/android-developer/answer/6346149) (Reviewed 2026-08-27).
- [Halt a fully rolled-out release](https://support.google.com/googleplay/android-developer/answer/16285429?hl=en-GB) (Reviewed 2026-08-27).
- [Google Play Android vitals](https://support.google.com/googleplay/android-developer/answer/9844486?hl=en) (Reviewed 2026-08-27).
- [Android application versioning](https://developer.android.com/studio/publish/versioning) (Reviewed 2026-08-27).
