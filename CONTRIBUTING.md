# Contributing to OBOE

Thank you for considering contributing to OBOE!

We welcome contributions from the community — bug reports, feature requests, documentation improvements, and code changes are all appreciated.

## How to Contribute

1. **Fork the repository** on GitHub
2. **Create a new branch** for your changes (`git checkout -b feature/amazing-feature`)
3. **Make your changes** following the code style below
4. **Test your changes** (run `./mvnw test`)
5. **Commit your changes** (`git commit -m 'Add some amazing feature'`)
6. **Push to the branch** (`git push origin feature/amazing-feature`)
7. **Open a Pull Request** against the `main` branch

## Development Setup

```bash
# Clone the repo
git clone https://github.com/EDIandXML/OBOE.git
cd OBOE

# Build and test
./mvnw clean install
```

## Code Style

- Java 8+ compatible
- Follow standard Maven project layout
- Use 4-space indentation
- Include Javadoc for public APIs
- Add or update tests for new features

## Reporting Bugs / Feature Requests

- Use the [GitHub Issues](https://github.com/EDIandXML/OBOE/issues) tab
- For bugs, include steps to reproduce, expected vs actual behavior
- For features, describe the use case and why it matters

## Pull Request Guidelines

- Keep changes focused on a single issue
- Update the CHANGELOG.md under the `[Unreleased]` section
- Ensure all tests pass
- Update documentation if needed

## Questions?

Feel free to open an issue or ask in the Discussions tab.

Thank you for helping make OBOE better! 🚀
