/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2014 Serge Rieder serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.db2.model.plan;

import org.jkiss.dbeaver.ext.db2.DB2Constants;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.Timestamp;

/**
 * DB2 EXPLAIN_OBJECT table
 * 
 * @author Denis Forveille
 */
public class DB2PlanObject extends DB2PlanNode {

    private String displayName;
    private String nodeName;

    private String objectSchema;
    private String objectName;
    private String objectType;

    private Timestamp createTime;
    private Timestamp statsTime;
    private Integer columnsCount;
    private Integer rowCount;
    private Integer width;
    private Long pages;
    private String distinct;
    private String tablespaceName;
    private Double overHead;
    private Double transferRate;
    private Integer prefetchSize;
    private Integer extentSize;
    private Double cluster;
    private Long nLeaf;
    private Integer nLevels;
    private Long fullKeyCard;
    private Long overFlow;
    private Long firstKeyCard;
    private Long first2KeyCard;
    private Long first3KeyCard;
    private Long first4KeyCard;
    private Long sequentialPages;
    private Integer density;
    private String statsSrc;
    private Double avgSequenceGap;
    private Double avgSequenceFetchGap;
    private Double avgSequencePages;
    private Double avgSequenceFetchPages;
    private Double avgRandomPages;
    private Double avgRandomFetchPages;
    private Long numRIDs;
    private Long numRIDsDeleted;
    private Long numEmptyLeafs;
    private Long activeBlocks;
    private Integer numDataPart;
    private String nullKeys;

    // ------------
    // Constructors
    // ------------

    public DB2PlanObject(JDBCResultSet dbResult)
    {

        this.objectSchema = JDBCUtils.safeGetStringTrimmed(dbResult, "OBJECT_SCHEMA");
        this.objectName = JDBCUtils.safeGetString(dbResult, "OBJECT_NAME");
        this.objectType = JDBCUtils.safeGetString(dbResult, "OBJECT_TYPE");

        this.createTime = JDBCUtils.safeGetTimestamp(dbResult, "CREATE_TIME");
        this.statsTime = JDBCUtils.safeGetTimestamp(dbResult, "STATISTICS_TIME");
        this.columnsCount = JDBCUtils.safeGetInteger(dbResult, "COLUMN_COUNT");
        this.rowCount = JDBCUtils.safeGetInteger(dbResult, "ROW_COUNT");
        this.width = JDBCUtils.safeGetInteger(dbResult, "WIDTH");
        this.pages = JDBCUtils.safeGetLong(dbResult, "PAGES");
        this.distinct = JDBCUtils.safeGetString(dbResult, "DISTINCT");
        this.tablespaceName = JDBCUtils.safeGetString(dbResult, "TABLESPACE_NAME");
        this.overHead = JDBCUtils.safeGetDouble(dbResult, "OVERHEAD");
        this.transferRate = JDBCUtils.safeGetDouble(dbResult, "TRANSFER_RATE");
        this.prefetchSize = JDBCUtils.safeGetInteger(dbResult, "PREFETCHSIZE");
        this.extentSize = JDBCUtils.safeGetInteger(dbResult, "EXTENTSIZE");
        this.cluster = JDBCUtils.safeGetDouble(dbResult, "CLUSTER");
        this.nLeaf = JDBCUtils.safeGetLong(dbResult, "NLEAF");
        this.nLevels = JDBCUtils.safeGetInteger(dbResult, "NLEVELS");
        this.fullKeyCard = JDBCUtils.safeGetLong(dbResult, "FULLKEYCARD");
        this.overFlow = JDBCUtils.safeGetLong(dbResult, "OVERFLOW");
        this.firstKeyCard = JDBCUtils.safeGetLong(dbResult, "FIRSTKEYCARD");
        this.first2KeyCard = JDBCUtils.safeGetLong(dbResult, "FIRST2KEYCARD");
        this.first3KeyCard = JDBCUtils.safeGetLong(dbResult, "FIRST3KEYCARD");
        this.first4KeyCard = JDBCUtils.safeGetLong(dbResult, "FIRST4KEYCARD");
        this.sequentialPages = JDBCUtils.safeGetLong(dbResult, "SEQUENTIAL_PAGES");
        this.density = JDBCUtils.safeGetInteger(dbResult, "DENSITY");
        this.statsSrc = JDBCUtils.safeGetString(dbResult, "STATS_SRC");
        this.avgSequenceGap = JDBCUtils.safeGetDouble(dbResult, "AVERAGE_SEQUENCE_GAP");
        this.avgSequenceFetchGap = JDBCUtils.safeGetDouble(dbResult, "AVERAGE_SEQUENCE_FETCH_GAP");
        this.avgSequencePages = JDBCUtils.safeGetDouble(dbResult, "AVERAGE_SEQUENCE_PAGES");
        this.avgSequenceFetchPages = JDBCUtils.safeGetDouble(dbResult, "AVERAGE_SEQUENCE_FETCH_PAGES");
        this.avgRandomFetchPages = JDBCUtils.safeGetDouble(dbResult, "AVERAGE_RANDOM_FETCH_PAGES");
        this.numRIDs = JDBCUtils.safeGetLong(dbResult, "NUMRIDS");
        this.numRIDsDeleted = JDBCUtils.safeGetLong(dbResult, "NUMRIDS_DELETED");
        this.numEmptyLeafs = JDBCUtils.safeGetLong(dbResult, "NUM_EMPTY_LEAFS");
        this.activeBlocks = JDBCUtils.safeGetLong(dbResult, "ACTIVE_BLOCKS");
        this.numDataPart = JDBCUtils.safeGetInteger(dbResult, "NUM_DATA_PART");
        this.nullKeys = JDBCUtils.safeGetString(dbResult, "NULLKEYS");

        nodeName = buildName(objectSchema, objectName);

        StringBuilder sb2 = new StringBuilder(64);
        sb2.append(objectType);
        sb2.append(": ");
        sb2.append(nodeName);
        displayName = sb2.toString();
    }

    public DB2PlanObject()
    {
    }

    // -----------------
    // Business Contract
    // -----------------

    @Override
    public String toString()
    {
        return displayName;
    }

    @Override
    public String getNodeName()
    {
        return nodeName;
    }

    // --------
    // Helpers
    // --------
    public static String buildName(String objectSchema, String objectName)
    {
        return objectSchema + "." + objectName;
    }

    public DB2PlanObject clone()
    {
        DB2PlanObject clone = new DB2PlanObject();

        clone.displayName = this.getDisplayName();
        clone.nodeName = this.getNodeName();
        // clone.objectSchema=this.getObjectSc();
        // clone.objectName=this.getObjectType()
        clone.objectType = this.getObjectType();
        clone.createTime = this.getCreateTime();
        clone.statsTime = this.getStatsTime();
        clone.columnsCount = this.getColumnsCount();
        clone.rowCount = this.getRowCount();
        clone.width = this.getWidth();
        clone.pages = this.getPages();
        clone.distinct = this.getDistinct();
        clone.tablespaceName = this.getDisplayName();
        clone.overHead = this.getOverHead();
        clone.transferRate = this.getTransferRate();
        clone.prefetchSize = this.getPrefetchSize();
        clone.extentSize = this.getExtentSize();
        clone.cluster = this.getCluster();
        clone.nLeaf = this.getnLeaf();
        clone.nLevels = this.getnLevels();
        clone.fullKeyCard = this.getFullKeyCard();
        clone.overFlow = this.getOverFlow();
        clone.firstKeyCard = this.getFirstKeyCard();
        clone.first2KeyCard = this.getFirst2KeyCard();
        clone.first3KeyCard = this.getFirst3KeyCard();
        clone.first4KeyCard = this.getFirst4KeyCard();
        clone.sequentialPages = this.getSequentialPages();
        clone.density = this.getDensity();
        clone.statsSrc = this.getStatsSrc();
        clone.avgSequenceGap = this.getAvgSequenceGap();
        clone.avgSequenceFetchGap = this.getAvgSequenceFetchGap();
        clone.avgSequencePages = this.getAvgSequencePages();
        clone.avgSequenceFetchPages = this.getAvgSequenceFetchPages();
        clone.avgRandomPages = this.getAvgRandomPages();
        clone.avgRandomFetchPages = this.getAvgRandomFetchPages();
        clone.numRIDs = this.getNumRIDs();
        clone.numRIDsDeleted = this.getNumRIDsDeleted();
        clone.numEmptyLeafs = this.getNumEmptyLeafs();
        clone.activeBlocks = this.getActiveBlocks();
        clone.numDataPart = this.getNumDataPart();
        clone.nullKeys = this.getNullKeys();

        return clone;
    }

    // ----------------
    // Properties
    // ----------------

    @Property(editable = false, viewable = true, order = 1)
    public String getDisplayName()
    {
        return displayName;
    }

    @Property(editable = false, viewable = true, order = 2, category = DB2Constants.CAT_PERFORMANCE)
    public Double getEstimatedCardinality()
    {
        return Double.valueOf(rowCount);
    }

    @Property(editable = false, viewable = false, order = 3)
    public String getObjectType()
    {
        return objectType;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_PERFORMANCE)
    public Integer getRowCount()
    {
        return rowCount;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_DATETIME)
    public Timestamp getCreateTime()
    {
        return createTime;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Timestamp getStatsTime()
    {
        return statsTime;
    }

    @Property(editable = false, viewable = false)
    public Integer getColumnsCount()
    {
        return columnsCount;
    }

    @Property(editable = false, viewable = false)
    public Integer getWidth()
    {
        return width;
    }

    @Property(editable = false, viewable = false)
    public String getDistinct()
    {
        return distinct;
    }

    @Property(editable = false, viewable = false)
    public String getTablespaceName()
    {
        return tablespaceName;
    }

    @Property(editable = false, viewable = false)
    public Integer getPrefetchSize()
    {
        return prefetchSize;
    }

    @Property(editable = false, viewable = false)
    public Integer getExtentSize()
    {
        return extentSize;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getPages()
    {
        return pages;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Double getOverHead()
    {
        return overHead;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Double getTransferRate()
    {
        return transferRate;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Double getCluster()
    {
        return cluster;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getnLeaf()
    {
        return nLeaf;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Integer getnLevels()
    {
        return nLevels;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getOverFlow()
    {
        return overFlow;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getFullKeyCard()
    {
        return fullKeyCard;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getFirstKeyCard()
    {
        return firstKeyCard;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getFirst2KeyCard()
    {
        return first2KeyCard;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getFirst3KeyCard()
    {
        return first3KeyCard;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getFirst4KeyCard()
    {
        return first4KeyCard;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Long getSequentialPages()
    {
        return sequentialPages;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public Integer getDensity()
    {
        return density;
    }

    @Property(editable = false, viewable = false, category = DB2Constants.CAT_STATS)
    public String getStatsSrc()
    {
        return statsSrc;
    }

    @Property(editable = false, viewable = false)
    public Double getAvgSequenceGap()
    {
        return avgSequenceGap;
    }

    @Property(editable = false, viewable = false)
    public Double getAvgSequenceFetchGap()
    {
        return avgSequenceFetchGap;
    }

    @Property(editable = false, viewable = false)
    public Double getAvgSequencePages()
    {
        return avgSequencePages;
    }

    @Property(editable = false, viewable = false)
    public Double getAvgSequenceFetchPages()
    {
        return avgSequenceFetchPages;
    }

    @Property(editable = false, viewable = false)
    public Double getAvgRandomPages()
    {
        return avgRandomPages;
    }

    @Property(editable = false, viewable = false)
    public Double getAvgRandomFetchPages()
    {
        return avgRandomFetchPages;
    }

    @Property(editable = false, viewable = false)
    public Long getNumRIDs()
    {
        return numRIDs;
    }

    @Property(editable = false, viewable = false)
    public Long getNumRIDsDeleted()
    {
        return numRIDsDeleted;
    }

    @Property(editable = false, viewable = false)
    public Long getNumEmptyLeafs()
    {
        return numEmptyLeafs;
    }

    @Property(editable = false, viewable = false)
    public Long getActiveBlocks()
    {
        return activeBlocks;
    }

    @Property(editable = false, viewable = false)
    public Integer getNumDataPart()
    {
        return numDataPart;
    }

    @Property(editable = false, viewable = false)
    public String getNullKeys()
    {
        return nullKeys;
    }

}
