package io.wauction.core.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.util.Objects;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomPrincipal implements Principal {

    private String name;
    private String channelId;
    private String role;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomPrincipal that)) return false;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getChannelId(), that.getChannelId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getChannelId());
    }
}
