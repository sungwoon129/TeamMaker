package io.wauction.core.channels.infrastructure;

import io.wauction.core.channels.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
}
