package com.InterestRatesAustria.InterestRatesAustria.service;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.LastUpdate;
import com.InterestRatesAustria.InterestRatesAustria.repository.LastUpdateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class LastUpdateService {

    private final LastUpdateRepository lastUpdateRepository;

    public LastUpdateService(LastUpdateRepository lastUpdateRepository) {
        this.lastUpdateRepository = lastUpdateRepository;
    }

    public void recordUpdate() {
        lastUpdateRepository.deleteAll();
        
        LastUpdate update = LastUpdate.builder()
                .lastUpdated(LocalDateTime.now())
                .build();
        
        lastUpdateRepository.save(update);
    }

    public String getFormattedLastUpdateMessage() {
        Optional<LastUpdate> lastUpdate = lastUpdateRepository.findMostRecent();
        
        if (lastUpdate.isEmpty()) {
            return "No updates recorded yet";
        }

        String timeAgo = getTimeAgoString(lastUpdate.get().getLastUpdated());
        return String.format("The interest rates were last updated %s", timeAgo);
    }

    public LocalDateTime getLastUpdateTime() {
        Optional<LastUpdate> lastUpdate = lastUpdateRepository.findMostRecent();
        return lastUpdate.map(LastUpdate::getLastUpdated).orElse(null);
    }

    private String getTimeAgoString(LocalDateTime updateTime) {
        LocalDateTime now = LocalDateTime.now();
        
        long minutes = ChronoUnit.MINUTES.between(updateTime, now);
        long hours = ChronoUnit.HOURS.between(updateTime, now);
        long days = ChronoUnit.DAYS.between(updateTime, now);
        long weeks = ChronoUnit.WEEKS.between(updateTime, now);
        long months = ChronoUnit.MONTHS.between(updateTime, now);
        long years = ChronoUnit.YEARS.between(updateTime, now);

        if (years > 0) {
            return years == 1 ? "1 year ago" : years + " years ago";
        } else if (months > 0) {
            return months == 1 ? "1 month ago" : months + " months ago";
        } else if (weeks > 0) {
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        } else if (days > 0) {
            return days == 1 ? "1 day ago" : days + " days ago";
        } else if (hours > 0) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (minutes > 0) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        } else {
            return "just now";
        }
    }
}