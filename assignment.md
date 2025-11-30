Aviation API Wrapper - BE Home Assignment
Name Surname

Overview
This exercise evaluates your ability to design and implement a production-ready
backend microservice. The focus is on how well your solution handles scalability,
resilience, and security, in addition to correctness.
You will build a Java + Spring Boot microservice that integrates with a public aviation
data API to retrieve information about airports based on ICAO codes.

Assignment Scope
Design and implement a microservice that:
●​ Accepts HTTP requests to fetch airport details using an ICAO code.
●​ Queries the public aviation data API at https://aviationapi.com to retrieve the data.
●​ The response format is up to you, but it should be clean and documented,
containing key airport information (e.g., name, location, ICAO/IATA, etc.).
●​ Handles upstream API failures gracefully (timeouts, retries, rate limits, etc.).
Use libraries/patterns you’d be comfortable supporting in production.
https://aviationapi.com/ is a good starting point for third-party data.
You are encouraged to spend around 90 minutes. Prioritize structure, clarity, and
resilience over polish - it’s acceptable to stub or omit some functionality if your design
decisions are well documented.
AI tools may be used to assist (e.g., code snippets or boilerplate), but you’re responsible
for verifying and adapting anything generated.​

Aviation API Wrapper - BE Home Assignment
Name Surname

What We’re Evaluating
We’re interested in your engineering decisions more than the exact feature set.
Key focus areas:
●​ Scalability: clean service layering, statelessness, readiness for load.
●​ Resilience: retry logic, circuit breakers, fallback strategies.
●​ Extensibility: your code should not be tightly coupled to one provider.
●​ Observability: basic logging, error transparency, metrics readiness.

Assumptions
●​ No frontend is required.
●​ You can assume the third-party API is accessible but may be unstable.
●​ Only the ICAO code lookup is in scope.
●​ No user management is required.​

Deliverables
●​ A public GitHub repo containing:
○​ Executable project (Maven/Gradle or Docker).
○​ A README that includes:
■​ Setup and run instructions.
■​ Instructions for running tests.
■​ Notes on your assumptions, architecture decisions, and error
handling.
■​ Details on AI-generated code, if used.
○​ At least one integration test.​

