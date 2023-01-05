package com.kontomatik.pko.domain;

import java.util.List;

public record AccountsInfo(
    List<AccountInfo> accounts
) {
}
