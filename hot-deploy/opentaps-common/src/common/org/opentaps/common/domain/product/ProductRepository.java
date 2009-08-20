/*
 * Copyright (c) 2008 - 2009 Open Source Strategies, Inc.
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
package org.opentaps.common.domain.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.domain.base.entities.ProductAssoc;
import org.opentaps.domain.product.Product;
import org.opentaps.domain.product.ProductRepositoryInterface;
import org.opentaps.foundation.entity.Entity;
import org.opentaps.foundation.entity.EntityNotFoundException;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.ofbiz.Repository;

/** {@inheritDoc} */
public class ProductRepository extends Repository implements ProductRepositoryInterface {

    /**
     * Default constructor.
     */
    public ProductRepository() {
        super();
    }

    /** {@inheritDoc} */
    public Product getProductById(String productId) throws RepositoryException, EntityNotFoundException {
        if (UtilValidate.isEmpty(productId)) {
            return null;
        }

        return findOneNotNull(Product.class, map(Product.Fields.productId, productId), "OpentapsError_ProductNotFound", UtilMisc.toMap("productId", productId));
    }

    /** {@inheritDoc} */
    public BigDecimal getUnitPrice(Product product, String currencyUomId) throws RepositoryException {
        return getUnitPrice(product, null, currencyUomId, null);
    }

    /** {@inheritDoc} */
    public BigDecimal getUnitPrice(Product product, BigDecimal quantity, String currencyUomId, String partyId) throws RepositoryException {

        try {
            Map<String, ?> results = getDispatcher().runSync("calculateProductPrice", UtilMisc.toMap(
                    "userLogin", getUser().getOfbizUserLogin(),
                    "product", Repository.genericValueFromEntity(product),
                    "partyId", partyId,
                    "quantity", quantity,
                    "currencyUomId", currencyUomId), -1, false);
            if (ServiceUtil.isError(results)) {
                throw new RepositoryException(ServiceUtil.getErrorMessage(results));
            }
            return (BigDecimal) results.get("price");
        } catch (GenericServiceException e) {
            throw new RepositoryException(e);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public BigDecimal getStandardCost(Product product, String currencyUomId) throws RepositoryException {
        try {
            Map results = getDispatcher().runSync("getProductCost", UtilMisc.toMap(
                    "userLogin", getUser().getOfbizUserLogin(),
                    "productId", product.getProductId(),
                    "currencyUomId", currencyUomId,
                    "costComponentTypePrefix", "EST_STD"));
            if (ServiceUtil.isError(results)) {
                throw new RepositoryException(ServiceUtil.getErrorMessage(results));
            }
            return (BigDecimal) results.get("productCost");
        } catch (GenericServiceException e) {
            throw new RepositoryException(e);
        }
    }

    /** {@inheritDoc} */
    public List<Product> getVariants(Product product) throws RepositoryException {
        EntityConditionList<EntityCondition> conditions = EntityCondition.makeCondition(EntityOperator.AND,
                EntityCondition.makeCondition(ProductAssoc.Fields.productId.name(), product.getProductId()),
                EntityCondition.makeCondition(ProductAssoc.Fields.productAssocTypeId.name(), "PRODUCT_VARIANT"),
                EntityUtil.getFilterByDateExpr());

        List<ProductAssoc> variants = findList(ProductAssoc.class, conditions);
        return findList(Product.class, EntityCondition.makeCondition(Product.Fields.productId.name(), EntityOperator.IN, Entity.getDistinctFieldValues(variants, ProductAssoc.Fields.productIdTo)));
    }
}
