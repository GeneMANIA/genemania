package org.genemania.adminweb.entity;

import org.genemania.adminweb.dao.impl.NetworkDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

// TODO: change to longs from ints (even though annoying, because our old schema has long ids for everything)

/**
 * either NETWORKS or ATTRIBUTES.
 */
@DatabaseTable(tableName = "NETWORKS", daoClass = NetworkDaoImpl.class)
public class Network {

    public static final String NAME_FIELD = "NAME";
    public static final String ORGANISM_ID_FIELD = "ORGANISM_ID";
    public static final String GROUP_ID_FIELD = "GROUP_ID";

    // expected values for type
    public static final String TYPE_NETWORK = "NETWORK";     // sparse network
    public static final String TYPE_ATTRIBUTE = "ATTRIBUTE"; // attribute set

    // expected values for status
    public static final String STATUS_ACTIVE = "ACTIVE";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = NAME_FIELD, canBeNull = false)
    private String name;

    @DatabaseField(columnName = "DESCRIPTION", canBeNull = true)
    private String description;

    @DatabaseField(columnName = ORGANISM_ID_FIELD, canBeNull = false, foreign = true)
    private Organism organism;

    @DatabaseField(columnName = "GROUP_ID", canBeNull = false, foreign = true)
    private Group group;

    @DatabaseField(columnName = "DATA_FILE_ID", canBeNull = true, foreign = true)
    private DataFile dataFile;

    @DatabaseField(columnName = "FORMAT_ID", canBeNull = true, foreign = true)
    private Format format;

    @DatabaseField(columnName = "PUBMED_ID", canBeNull = true)
    private long pubmedId;

    @DatabaseField(columnName = "SOURCE")
    private String source;

    @DatabaseField(columnName = "USER")
    private String user;

    @DatabaseField(columnName = "COMMENT", width = 4000)
    private String comment;

    @DatabaseField(columnName = "IS_DEFAULT")
    private boolean isDefault;

    @DatabaseField(columnName = "RESTRICTED_LICENSE")
    private boolean restrictedLicense;

    // use controlled flag to include or exclude a network
    // in a build
    @DatabaseField(columnName = "ENABLED", canBeNull = false, defaultValue = "1")
    private boolean enabled;

    // system status flag, validated, failed validation, etc. TODO defind
    @DatabaseField(columnName = "STATUS", canBeNull = false)
    private String status;

    // extra metadata, stored as json string
    @DatabaseField(columnName = "EXTRA", canBeNull = true, width = 2048)
    private String extra;

    // either NETWORK or ATTRIBUTE
    @DatabaseField(columnName = "TYPE", canBeNull = false, width = 16)
    private String type;

    @DatabaseField(columnName = "ATTRIBUTE_METADATA_ID", canBeNull = true, foreign = true)
    private AttributeMetadata attributeMetadata;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public long getPubmedId() {
        return pubmedId;
    }

    public void setPubmedId(long pubmedId) {
        this.pubmedId = pubmedId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Network() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isRestrictedLicense() {
        return restrictedLicense;
    }

    public void setRestrictedLicense(boolean restrictedLicense) {
        this.restrictedLicense = restrictedLicense;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Network other = (Network) obj;
        if (id != other.id)
            return false;
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public AttributeMetadata getAttributeMetadata() {
        return attributeMetadata;
    }

    public void setAttributeMetadata(AttributeMetadata attributeMetadata) {
        this.attributeMetadata = attributeMetadata;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

}
