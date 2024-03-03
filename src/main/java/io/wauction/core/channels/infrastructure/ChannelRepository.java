package io.wauction.core.channels.infrastructure;

import io.wauction.core.channels.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    List<Channel> findByDeletedIsNullOrDeletedFalse();
}
