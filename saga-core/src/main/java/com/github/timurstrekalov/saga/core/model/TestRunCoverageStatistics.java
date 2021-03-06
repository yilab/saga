package com.github.timurstrekalov.saga.core.model;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.timurstrekalov.saga.core.Order;
import com.github.timurstrekalov.saga.core.SortBy;
import com.github.timurstrekalov.saga.core.util.MiscUtil;
import com.github.timurstrekalov.saga.core.util.UriUtil;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Contains coverage information generated by a single test run.
 */
public final class TestRunCoverageStatistics implements Iterable<ScriptCoverageStatistics> {

    public static final TestRunCoverageStatistics EMPTY = new TestRunCoverageStatistics(null, null);

    public final URI test;
    public final String title;

    private SortBy sortBy;
    private Order order;

    private final Map<URI, ScriptCoverageStatistics> fileStatsMap = Maps.newTreeMap();

    private List<String> sourceDirs;

    public TestRunCoverageStatistics(final URI test) {
        this(test, String.format("Coverage report for \"%s\"", test));
    }

    public TestRunCoverageStatistics(final URI test, final String title) {
        this.test = test;
        this.title = title;
    }

    public String getTestName() {
        return UriUtil.getLastSegmentOrHost(test);
    }

    public void add(final ScriptCoverageStatistics newStats) {
        final URI key = newStats.getFileUri();
        final ScriptCoverageStatistics oldStats = fileStatsMap.get(key);

        if (oldStats != null) {
            fileStatsMap.put(key, ScriptCoverageStatistics.merge(newStats, oldStats));
        } else {
            fileStatsMap.put(key, newStats);
        }
    }

    public List<ScriptCoverageStatistics> getFileStats() {
        final List<ScriptCoverageStatistics> result = Lists.newLinkedList(fileStatsMap.values());

        Collections.sort(result, new Comparator<ScriptCoverageStatistics>() {
            @Override
            public int compare(final ScriptCoverageStatistics s1, final ScriptCoverageStatistics s2) {
                return (getOrder() == Order.ASC ? 1 : -1) * getSortBy().compare(s1, s2);
            }
        });

        return result;
    }

    public Collection<ScriptCoverageStatistics> getFileStatsWithSeparateFileOnly() {
        return Collections2.filter(getFileStats(), new Predicate<ScriptCoverageStatistics>() {
            @Override
            public boolean apply(final ScriptCoverageStatistics stats) {
                return stats.isSeparateFile();
            }
        });
    }

    public int getTotalStatements() {
        return MiscUtil.sum(fileStatsMap.values(), new Function<ScriptCoverageStatistics, Integer>() {

            @Override
            public Integer apply(final ScriptCoverageStatistics input) {
                return input.getStatements();
            }
        });
    }

    public int getTotalExecuted() {
        return MiscUtil.sum(fileStatsMap.values(), new Function<ScriptCoverageStatistics, Integer>() {

            @Override
            public Integer apply(final ScriptCoverageStatistics input) {
                return input.getExecuted();
            }
        });
    }

    public int getTotalCoverage() {
        return MiscUtil.toCoverage(getTotalStatements(), getTotalExecuted());
    }

    public double getTotalCoverageRate() {
        return MiscUtil.toCoverageRate(getTotalStatements(), getTotalExecuted());
    }

    public boolean getHasStatements() {
        return getTotalStatements() > 0;
    }

    public String getBarColor() {
        return MiscUtil.getColor(getTotalCoverage());
    }

    public int getBarColorAsArgb() {
        return MiscUtil.getColorAsArgb(getTotalCoverage());
    }

    @Override
    public Iterator<ScriptCoverageStatistics> iterator() {
        return getFileStats().iterator();
    }

    public void setSortBy(final SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public void setOrder(final Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void setSourceDirs(List<String> sourceDirs) {
        this.sourceDirs = sourceDirs;
    }

    public List<String> getSourceDirs(){
        return sourceDirs;
    }

}
