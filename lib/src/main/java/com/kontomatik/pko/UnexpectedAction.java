package com.kontomatik.pko;

import java.util.Collections;
import java.util.List;

public class UnexpectedAction extends RuntimeException {
    public final String lastStep;
    private final List<PkoLoginUseCase.Success> successful;
    private final List<PkoLoginUseCase.Fail> failed;

    public UnexpectedAction(
        String lastStep,
        List<PkoLoginUseCase.Success> successful,
        List<PkoLoginUseCase.Fail> failed
    ) {
        super(String.format(
            "Failed assertion for step [%s] result. Successful assertions: [%s]. Failed assertions: [%s]",
            lastStep,
            successful.stream().map(PkoLoginUseCase.Success::message).toList(),
            failed.stream().map(PkoLoginUseCase.Fail::message).toList()
        ));
        this.lastStep = lastStep;
        this.successful = successful;
        this.failed = failed;
    }

    public List<PkoLoginUseCase.Success> getSuccessful() {
        return Collections.unmodifiableList(successful);
    }

    public List<PkoLoginUseCase.Fail> getFailed() {
        return Collections.unmodifiableList(failed);
    }

    public static UnexpectedAction fromAssertionResults(
        String lastStep,
        List<PkoLoginUseCase.AssertionResult> assertionResults
    ) {
        var successful = assertionResults.stream()
            .filter(it -> it instanceof PkoLoginUseCase.Success)
            .map(it -> (PkoLoginUseCase.Success) it)
            .toList();

        var failed = assertionResults.stream()
            .filter(it -> it instanceof PkoLoginUseCase.Fail)
            .map(it -> (PkoLoginUseCase.Fail) it)
            .toList();

        return new UnexpectedAction(lastStep, successful, failed);
    }
}


