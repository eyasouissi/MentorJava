package tn.esprit.services;

// YearWeek.java
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Objects;

public final class YearWeek implements Comparable<YearWeek> {
    private final int year;
    private final int week;

    private YearWeek(int year, int week) {
        this.year = year;
        this.week = week;
    }

    public static YearWeek of(int year, int week) {
        return new YearWeek(year, week);
    }

    public static YearWeek from(LocalDate date) {
        return new YearWeek(date.getYear(), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    public int getYear() {
        return year;
    }

    public int getWeek() {
        return week;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YearWeek yearWeek = (YearWeek) o;
        return year == yearWeek.year && week == yearWeek.week;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, week);
    }

    @Override
    public int compareTo(YearWeek other) {
        if (this.year != other.year) {
            return Integer.compare(this.year, other.year);
        }
        return Integer.compare(this.week, other.week);
    }

    @Override
    public String toString() {
        return String.format("%d-W%02d", year, week);
    }
}
