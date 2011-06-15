/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010-2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.testing.cache;

import org.jboss.logging.Logger;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cache.spi.access.RegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.internal.CoreMessageLogger;

/**
 * @author Strong Liu
 */
abstract class BaseRegionAccessStrategy implements RegionAccessStrategy {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class, BaseRegionAccessStrategy.class.getName()
	);

	protected abstract BaseGeneralDataRegion getInternalRegion();
	protected abstract boolean isDefaultMinimalPutOverride();
	@Override
	public Object get(Object key, long txTimestamp) throws CacheException {
		return getInternalRegion().get( key );
	}

	@Override
	public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version) throws CacheException {
		return putFromLoad( key, value, txTimestamp, version, isDefaultMinimalPutOverride() );
	}

	@Override
	public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
			throws CacheException {

		if ( key == null || value == null ) {
			return false;
		}
		if ( minimalPutOverride && getInternalRegion().contains( key ) ) {
			LOG.debugf( "Item already cached: %s", key );
			return false;
		}
		LOG.debugf( "Caching: %s", key );
		getInternalRegion().put( key, value );
		return true;

	}

	@Override
	public SoftLock lockItem(Object key, Object version) throws CacheException {
		return null;
	}

	@Override
	public SoftLock lockRegion() throws CacheException {
		return null;
	}

	@Override
	public void unlockItem(Object key, SoftLock lock) throws CacheException {
	}

	@Override
	public void unlockRegion(SoftLock lock) throws CacheException {
		evictAll();
	}

	@Override
	public void remove(Object key) throws CacheException {
		evict( key );
	}

	@Override
	public void removeAll() throws CacheException {
		evictAll();
	}

	@Override
	public void evict(Object key) throws CacheException {
		getInternalRegion().evict( key );
	}

	@Override
	public void evictAll() throws CacheException {
		getInternalRegion().evictAll();
	}
}