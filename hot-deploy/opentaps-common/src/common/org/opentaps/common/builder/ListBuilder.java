package org.opentaps.common.builder;

/*
 * Copyright (c) 2006 - 2009 Open Source Strategies, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Honest Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Honest Public License for more details.
 *
 * You should have received a copy of the Honest Public License
 * along with this program; if not, write to Funambol,
 * 643 Bair Island Road, Suite 305 - Redwood City, CA 94063, USA
 */

import java.util.List;

/**
 * Interface for list builders.  A list builder encapsulates the
 * process of getting a set of data for a view.   The primary
 * intent is to provide a consistent pattern and location for
 * the code that generates lists of data.
 *
 * This builder is a 0 based list, which means the first element
 * will have index 0. 
 *
 * It is hoped that this will replace the traditional process 
 * where we would define a lookup in a beanshell script or 
 * in a service service and then are forced to refactor it into 
 * a Java util method in some random package.  Thus, this
 * can also be viewed as an effort to simplify the management
 * of data building code.
 *
 * The degenerate example would be an EntityListBuilder, which
 * encapsulates the common process of setting up a lookup 
 * on an entity or view entity using
 * GenericDelegator.findListIteratorByCondition().
 *
 * As a further example to highlight the advantages of this pattern,
 * if we were to have a lookup that does additional processing
 * after the EntityListIterator is retrieved, we can build a custom
 * ListBuilder for it by extending EntityListBuilder and overriding
 * the getPartialList() method.
 *
 * To further help in designing builders, the vision of this class is
 * to allow for the construction of any imaginable list via any
 * kind of process.  Imagine if you had to query a remote service
 * to get a real-time list of currency exchange rates.  You might design
 * a RemoteCurrencyExchangeBuilder to intelligently poll the service
 * for data and cache it, then serve the data using the interface methods
 * defined here.  
 *
 * You can then use this builder from within a service that runs at regular 
 * intervals and translates the remote data and stores it in the database.  
 *
 * As an alternative, you might present a user with the current exchange
 * values in the database and the remote values so that she may compare
 * them and decide whether to update the database values.* In this case, 
 * you would use the RemoteCurrencyExchangeBuilder and an EntityListBuilder 
 * to generate the two views.  The update service can also use the
 * RemoteCurrencyExchangeBuilder to get the data.
 */
public interface ListBuilder {

    /**
     * Initializes the list builder so that the interface get functions
     * are ready to be used.  Constructors should not call this method.
     *
     * @throws ListBuilderException
     */
    void initialize() throws ListBuilderException;

    /** Query whether the builder is initialized and ready to go. */
    boolean isInitialized();

    /**
     * Release all resources that the builder might be using.
     */
    void close();

    /**
     * Provides the size of the list.  For most lists, this is a simple
     * algorithm.  Some lists require a special heuristic to determine the
     * list size, and a few rare ones cannot provide a deterministic
     * result and are forced to guess.  This function should be implemented
     * in a way that is intelligently cached so it can be called multiple
     * times without much performance impact.
     *
     * @throws ListBuilderException
     */
    long getListSize() throws ListBuilderException;

    /**
     * If the list size is not deterministic, then consumers of the builder
     * might want to render the whole list or do something different.
     *
     * @throws ListBuilderException
     */
    boolean isDeterministic() throws ListBuilderException;

    /**
     * Build a page given the size of the view and the cursor position.
     * This is the main method invoked by Paginator to get a list.  It will
     * call getPartialList to construct the actual data, then use a PageBuilder
     * to operate on the data if it is defined.  Only one implementation is necessary.
     */
    List build(long viewSize, long cursorIndex) throws ListBuilderException;

    /**
     * Get a subset of the list starting from the index of the cursor
     * with a number of elements equal to the view size.  All of the
     * data retrieval and construction occurs within this method.
     *
     * @throws ListBuilderException
     */
    List getPartialList(long viewSize, long cursorIndex) throws ListBuilderException;

    /**
     * Get the whole list.  Only one implementation is required for deterministic
     * lists.  Non deterministic lists should define this.
     */
    List getCompleteList() throws ListBuilderException;

    /**
     * This method handles anything that needs to happen when
     * the order of a list changes.
     */
    void changeOrderBy(List orderBy);

    /**
     * Whether the list builder has a page builder or not.  Requires only one implementation.
     */
    boolean hasPageBuilder();

    /**
     * Set the page builder.  Requires only one implementation.
     */
    void setPageBuilder(PageBuilder builder);

    /**
     * Get the page builder.  Requires only one implementation.
     */
    PageBuilder getPageBuilder();

}
