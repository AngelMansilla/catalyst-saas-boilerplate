# ADR-001: Angular over Next.js for Frontend Framework

## Status
Accepted

## Date
2026-01-15

## Context
The project initially considered Next.js 15 (React-based) for the frontend. After evaluating both options against the project's requirements, a decision was made to switch to Angular.

### Options Considered
1. **Next.js 15** (React) — SSR/SSG, large ecosystem, flexible but less opinionated.
2. **Angular 21** — Full-featured framework, strongly opinionated, excellent TypeScript integration.

## Decision
**Angular 21** with Angular Material was selected as the frontend framework.

### Rationale
1. **Enterprise-grade structure** — Angular's opinionated architecture (modules, services, dependency injection) aligns with the backend's hexagonal architecture philosophy.
2. **TypeScript-first** — Angular is built with TypeScript from the ground up, providing superior type safety compared to React's opt-in TypeScript support.
3. **Built-in tooling** — Angular CLI, built-in testing (Karma/Jest), and official libraries (Angular Material, CDK) reduce decision fatigue.
4. **Signals and modern reactivity** — Angular 21's Signals provide a clean, performant state management model without needing external libraries (Redux, Zustand, etc.).
5. **Long-term stability** — Angular's predictable release cycle and backwards compatibility guarantees suit a boilerplate meant to last.
6. **Official component library** — Angular Material provides a cohesive, accessible UI component set with minimal configuration.

## Consequences
- Frontend developers must know Angular (smaller talent pool than React).
- No SSR/SSG out of the box (Angular Universal exists but is less mature than Next.js).
- Tighter coupling to the Angular ecosystem for UI components and tooling.
- Stronger architectural consistency between frontend and backend.
