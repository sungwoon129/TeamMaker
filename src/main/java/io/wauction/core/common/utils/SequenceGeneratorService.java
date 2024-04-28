package io.wauction.core.common.utils;

import io.wauction.core.auction.entity.AuctionOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
@Service
public class SequenceGeneratorService {
    private final MongoOperations mongoOperations;


    public long generateSequence(String seqName) {
        AuctionOrder counter = mongoOperations.findAndModify(query(where("_id").is(seqName)),
                new Update().inc("seq",1),
                options().returnNew(true).upsert(true),
                AuctionOrder.class
        );
        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }
}
