package com.kontomatik.pko.domain;

import java.util.Collections;
import java.util.List;

public class UnexpectedAction extends RuntimeException {
    public final String lastStep;
    private final List<String> successful;
    private final List<String> failed;

    public UnexpectedAction(String lastStep, List<String> successful, List<String> failed) {
        super(String.format(
            "Failed assertion for step [%s] result. Successful assertions: [%s]. Failed assertions: [%s]",
            lastStep,
            successful,
            failed
        ));
        this.lastStep = lastStep;
        this.successful = successful;
        this.failed = failed;
    }

    public List<String> getSuccessful() {
        return Collections.unmodifiableList(successful);
    }

    public List<String> getFailed() {
        return Collections.unmodifiableList(failed);
    }

    public static UnexpectedAction fromAssertionResults(
        String lastStep,
        List<PkoLoginUseCase.AssertionResult> assertionResults
    ) {
        var successful = assertionResults.stream()
            .filter(it -> it instanceof PkoLoginUseCase.Success)
            .map(PkoLoginUseCase.AssertionResult::message)
            .toList();

        var failed = assertionResults.stream()
            .filter(it -> it instanceof PkoLoginUseCase.Fail)
            .map(PkoLoginUseCase.AssertionResult::message)
            .toList();

        return new UnexpectedAction(lastStep, successful, failed);
    }
}


