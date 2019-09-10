// Copyright 2006-2018, by the California Institute of Technology.
// ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
// Any commercial use must be negotiated with the Office of Technology Transfer
// at the California Institute of Technology.
//
// This software is subject to U. S. export control laws and regulations
// (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
// is subject to U.S. export control laws and regulations, the recipient has
// the responsibility to obtain export licenses or other export authority as
// may be required before exporting such information to foreign countries or
// providing access to foreign nationals.
//
// $Id: LidVid.java 10921 2012-09-10 22:11:40Z mcayanan $
package gov.nasa.pds.tools.util;

/**
 * Class that represents the lidvid of a PDS4 data product.
 *
 * @author mcayanan
 *
 */
public class ContextProductReference {

    /** The logical identifier. */
    private String lid;

    /** The version. */
    private String version;

    /** The type. */
    private String type;

    /** The name. */
    private String name;

    /** Flag to indicate if a version exists. */
    private boolean hasVersion;

    public ContextProductReference(String lid) {
        this(lid, null, null, null);
    }

    public ContextProductReference(String lid, String version, String type, String name) {
        this.lid = lid;
        this.version = version;
        this.type = type;
        this.name = name;
        if (this.version == null) {
            hasVersion = false;
        } else {
            hasVersion = true;
        }
        // System.out.println(getLid() + ", " + getVersion() + ", " + getName()
        // + ", " + getType());
    }

    public String getLid() {
        return this.lid;
    }

    public String getVersion() {
        return this.version;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }
    
    public void setLid(String lid) {
        this.lid = lid;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasVersion() {
        return this.hasVersion;
    }

    public String toString() {
        String identifier = this.lid;
        if (hasVersion) {
            identifier += "::" + this.version;
        }
        return identifier;
    }

    /**
     * Determines where 2 LIDVIDs are equal.
     *
     */
    public boolean equals(Object o) {
        boolean isEqual = false;
        ContextProductReference lidvid = (ContextProductReference) o;

        // if(lidvid.getName().equals("N/A"))
        // System.out.println("obj 1: " + lidvid.getLid() + ", " +
        // lidvid.getVersion() + ", " + lidvid.getName() + ", " +
        // lidvid.getType());

        // Compare Lid and/or version only
        if (this.lid.equalsIgnoreCase(lidvid.getLid())) {
            if (this.hasVersion) {
                if (lidvid.hasVersion() && this.version.equals(lidvid.getVersion())) {
                    isEqual = true;
                }
            } else {
                isEqual = true;
            }
        }
        return isEqual;
    }
}
