package io.wauction.core.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomPrincipal implements Principal {

    private String name;
    @Override
    public String getName() {
        return this.name;
    }
}
