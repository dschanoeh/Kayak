/**
 * 	This file is part of Kayak.
 *
 *	Kayak is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Kayak is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Kayak.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.kayak.core.description;

import java.util.HashSet;

/**
 *
 * @author Jan-Niklas Meier < dschanoeh@googlemail.com >
 */
public class Document {

    private HashSet<Node> nodes;
    private HashSet<BusDescription> busses;
    private String name;
    private String version;
    private String author;
    private String company;
    private String date;
    private String fileName;

    public HashSet<Node> getNodes() {
        return nodes;
    }

    public void setNodes(HashSet<Node> nodes) {
        this.nodes = nodes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public HashSet<BusDescription> getBusses() {
        return busses;
    }

    public BusDescription createBusDescription() {
        BusDescription b = new BusDescription(this);
        
        return b;
    }
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Document() {
        nodes = new HashSet<Node>();
        busses = new HashSet<BusDescription>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
