/*
 * Copyright (c) 2007 - 2010 Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opentaps.domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.LocalDispatcher;
import org.opentaps.foundation.domain.DomainInterface;
import org.opentaps.foundation.infrastructure.DomainContextInterface;
import org.opentaps.foundation.infrastructure.Infrastructure;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.infrastructure.User;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * This class helps with loading of domains using the Spring framework.
 */
public class DomainsLoader implements DomainContextInterface {

    private static String DOMAINS_DIRECTORY_FILE = "domains-directory.xml";
    private static String DOMAINS_DIRECTORY_BEAN_ID = "domainsDirectory";
    private static String MODULE = DomainsLoader.class.getName();
   
    /* The domainsDirectory is static because the XML definitions should not
     * change until the system restarts. Also, the directory will accept 
     * registrations from various modules.  By having a single, centralized
     * directory all domains can access all other domains.
     * 
     * To support separate domains directories for each component, we maintain
     * a Map of source file -> DomainsDirectory
     */
    private static Map<String,DomainsDirectory> domainsDirectories = FastMap.newInstance();
    private static Set<String> registeredLoaders = new HashSet<String>();
    
	private Infrastructure infrastructure = null;
    private User user = null;

    public DomainsLoader() {
        // default constructor
    }

    /**
     * Creates a new <code>DomainsLoader</code> instance.
     *
     * @param infrastructure an <code>Infrastructure</code> value
     * @param user an <code>User</code> value
     */
    public DomainsLoader(Infrastructure infrastructure, User user) {
        this();
        setInfrastructure(infrastructure);
        setUser(user);
    }
    
    public DomainsLoader(Infrastructure infrastructure, User user, String domainsDirectoryFile) {
        this();
        setInfrastructure(infrastructure);
        setUser(user);
    }

    /**
     * Constructs a domain loader from an application's request context.
     * @param request a <code>HttpServletRequest</code> value
     * @exception InfrastructureException if an error occurs
     */
    public DomainsLoader(HttpServletRequest request) throws InfrastructureException {
        this();

        // in OFBiz, we can get the dispatcher and user login as follows
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getAttribute("userLogin");
        if (userLogin == null) {
            HttpSession session = request.getSession();
            if (session != null) {
                userLogin = (GenericValue) session.getAttribute("userLogin");
            }
        }
        setInfrastructure(new Infrastructure(dispatcher));
        if (userLogin != null) {
            setUser(new User(userLogin));
        }
    }

    /** {@inheritDoc} */
    public void setUser(User user) {
        this.user = user;
    }

    /** {@inheritDoc} */
    public void setInfrastructure(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }

    /** {@inheritDoc} */
    public User getUser() {
        return this.user;
    }

    /** {@inheritDoc} */
    public Infrastructure getInfrastructure() {
        return this.infrastructure;
    }

    /** {@inheritDoc} */
    public void setDomainContext(DomainContextInterface context) {
        this.setDomainContext(context.getInfrastructure(), context.getUser());
    }

    /** {@inheritDoc} */
    public void setDomainContext(Infrastructure infrastructure, User user) {
        this.setInfrastructure(infrastructure);
        this.setUser(user);
    }
    
    /**
     * Returns the domains directory from the default DOMAINS_DIRECTORY_FILE. 
     * Overload in your extended class to return the custom domains for your class
     * 
     * @return
     */
    public DomainsDirectory getDomainsDirectory() {
    	return getDomainsDirectory(DOMAINS_DIRECTORY_FILE);
    }
    
    /**
     * Returns DomainsDirectory from your domainsDirectoryFile
     * Use this method to create overloaded versions in custom domains loaders
     * @param domainsDirectoryFile
     * @return
     */
    public DomainsDirectory getDomainsDirectory(String domainsDirectoryFile) {
    	if (domainsDirectories.get(domainsDirectoryFile) == null) {
    		initializeDomainsDirectory(infrastructure, user, domainsDirectoryFile);
    	}
    	
		return domainsDirectories.get(domainsDirectoryFile);
    }
    
    /**
     * Initialize the DomainsDirectory for domainsDirectoryFile.  This is internally
     * synchronized to ensure two threads do not attempt to initialize two
     * copies of the domainsDirectory from the same domainsDirectoryFile.  
     *
     * @param infrastructure
     * @param user
     * @param domainsDirectoryFile
     */
    private static synchronized void initializeDomainsDirectory(Infrastructure infrastructure, User user, String domainsDirectoryFile) {
    	/* If this method was not synchronized it could lead to a temporary
    	 * state of multiple domainsDirectories.  Registrations could randomly 
    	 * occur against either.  When the earlier of two was garbage collected 
    	 * the registrations within that instance would be lost.
    	 */
		if (domainsDirectories.get(domainsDirectoryFile) != null) {
			Debug.logWarning("Domains directory for [" + domainsDirectoryFile + "] is not null, will not be reinitializing", MODULE);
			return;
		}
		
		// use default domains directory file unless another one has been set
		if (domainsDirectoryFile == null) {
		    Debug.logFatal("No domains directory file found, using default value [" + DOMAINS_DIRECTORY_FILE + "]", MODULE);
			domainsDirectoryFile = DOMAINS_DIRECTORY_FILE;
		}
		
		Debug.logFatal("Using domains directory file [" + domainsDirectoryFile + "]", MODULE);
        Resource resource = new ClassPathResource(domainsDirectoryFile);
    	ListableBeanFactory bf = new XmlBeanFactory(resource);
    	DomainsDirectory myDomainsDirectory = (DomainsDirectory) bf.getBean(DOMAINS_DIRECTORY_BEAN_ID);
    	myDomainsDirectory.setInfrastructure(infrastructure);
    	myDomainsDirectory.setUser(user);
    	
    	// save using the domains directory file as key
    	domainsDirectories.put(domainsDirectoryFile, myDomainsDirectory);
    }
    
    
    /**
     * Registers the domains configured in the specified domainsDirectoryFile.
     * Extending DomainsLoaders should invoke this method on instantiation. This
     * method will ignore attempts to re-register domains that have already
     * registered.
     *
     * @param domainsDirectoryFile
     */
    protected void registerDomains(String domainsDirectoryFile) {
    	/* Figure out the name of the DomainLoader attempting to register. If it
    	 * has already registered, do not re-register
    	 */
    	final Throwable t = new Throwable();
        final StackTraceElement methodCaller = t.getStackTrace()[1];
        final String domainLoaderName = methodCaller.getClassName();
        if (registeredLoaders.contains(domainLoaderName)) {
        	// TODO: should throw an exception here
        	Debug.logWarning("Domain loader [" + domainLoaderName + "] has already been registered.  Will not be registering again.", MODULE);
        	return;
        } else {
        	Debug.logFatal("Now registering domain loader [" + domainLoaderName + "]", MODULE);
        }
        
        Resource resource = new ClassPathResource(domainsDirectoryFile);
        XmlBeanFactory bean = new XmlBeanFactory(resource);
    	String[] domainsToRegister = bean.getBeanNamesForType(DomainInterface.class);
    	DomainsDirectory domainsDirectory = getDomainsDirectory();
    	for (String domainToRegister : domainsToRegister) {
    		domainsDirectory.addDomain(domainToRegister, 
    				(DomainInterface) bean.getBean(domainToRegister));
    	}
    	
    	// Register the calling class so that it may not re-register
    	registeredLoaders.add(domainLoaderName);
    }

    /**
     * the domains directory by using the Spring framework to load the
     * domains directory file (by default called) "domains-directory.xml") and return its DomainsDirectory bean (by default called
     * "domainsDirectory")
     * @return
     * 
     * @deprecated {@link #getDomainsDirectory()}
     */
    public DomainsDirectory loadDomainsDirectory() {
    	return getDomainsDirectory();
    }
}
