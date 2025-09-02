package com.InterestRatesAustria.InterestRatesAustria.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class DateUtilService {

    public String getTimeAgoMessage(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        long weeks = ChronoUnit.WEEKS.between(dateTime, now);
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        long years = ChronoUnit.YEARS.between(dateTime, now);

        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        } else if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (days < 7) {
            return days == 1 ? "1 day ago" : days + " days ago";
        } else if (weeks < 4) {
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        } else if (months < 12) {
            return months == 1 ? "1 month ago" : months + " months ago";
        } else {
            return years == 1 ? "1 year ago" : years + " years ago";
        }
    }

    public String getLastUpdatedMessage(LocalDateTime lastUpdated) {
        if (lastUpdated == null) {
            return "The interest rates table has never been updated.";
        }
        
        String timeAgo = getTimeAgoMessage(lastUpdated);
        return "The interest rates table was last updated " + timeAgo + ".";
    }
}