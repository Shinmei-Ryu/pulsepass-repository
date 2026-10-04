package com.pulse.pass.mapper;

import com.pulse.pass.domain.Artist;
import com.pulse.pass.domain.Event;
import com.pulse.pass.dto.response.EventResponse;
import com.pulse.pass.dto.response.EventSummaryResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    EventResponse toResponse(Event event);

    @Mapping(target = "venueName", source = "venue.name")
    EventSummaryResponse toSummary(Event event);

    default List<String> mapArtists(Set<Artist> artists) {
        return artists.stream()
                .map(Artist::getStageName)
                .toList();
    }
}