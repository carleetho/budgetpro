# Baseline Establishment Procedures

> **Goal**: Know where we started to prove improvement.

## 1. AI Assumption Rate Baseline (2 Weeks)

1. **Tag**: `ai-generated` on PRs.
2. **Count**: Every time a reviewer says "This logic is wrong/invented".
3. **Calculate**: `Errors / Total AI PRs`.

## 2. Velocity Baseline (Audit)

1. **Survey**: Ask 3 senior devs: "How long do you spend digging for answers per feature?"
2. **Log**: Track the next 3 features. Time from "Picked Up" to "First Commit".

## 3. Onboarding Baseline (Historical)

1. **Review**: Look at the last 3 hires.
2. **Measure**: Days between `Start Date` and `Merged Feature PR`.

## 4. Rework Baseline (Jira/Git)

1. **Query**: Pull PRs from last quarter.
2. **Filter**: Comments containing "spec", "requirement", "wrong".
3. **Rate**: `Flagged PRs / Total PRs`.
