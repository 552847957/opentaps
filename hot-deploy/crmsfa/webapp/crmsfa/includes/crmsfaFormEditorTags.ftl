<#--
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
-->

<#function getTags>

  <#assign tags = []/>

  <#assign tags = tags + [{ r"${firstName}"                                : "${uiLabelMap.PartyFirstName}"                 }]/>
  <#assign tags = tags + [{ r"${lastName}"                                 : "${uiLabelMap.PartyLastName}"                  }]/>
  <#assign tags = tags + [{ r"${groupName}"                                : "${uiLabelMap.OpentapsCompanyName}"            }]/>
  <#assign tags = tags + [{ r"${fullName}"                                 : "${uiLabelMap.OpentapsCompanyOrPersonalName}"  }]/>
  <#assign tags = tags + [{ r"${salutation}"                               : "${uiLabelMap.CrmSalutation}"                  }]/>
  <#assign tags = tags + [{ r"${generalProfTitle}"                         : "${uiLabelMap.PartyPersonalTitle}"             }]/>
  <#assign tags = tags + [{ r"${email}"                                    : "${uiLabelMap.CrmPrimaryEmail}"                }]/>
  <#assign tags = tags + [{ r"${attnName}"                                 : "${uiLabelMap.CrmFormTagAttnTo}"               }]/>
  <#assign tags = tags + [{ r"${toName}"                                   : "${uiLabelMap.CrmFormTagTo}"                   }]/>
  <#assign tags = tags + [{ r"${address1}"                                 : "${uiLabelMap.CrmFormTagLine1}"                }]/>
  <#assign tags = tags + [{ r"${address2}"                                 : "${uiLabelMap.CrmFormTagLine2}"                }]/>
  <#assign tags = tags + [{ r"${city}"                                     : "${uiLabelMap.CrmFormTagCity}"                 }]/>
  <#assign tags = tags + [{ r"${state}"                                    : "${uiLabelMap.CrmFormTagState}"                }]/>
  <#assign tags = tags + [{ r"${zip}"                                      : "${uiLabelMap.CrmFormTagPostalCode}"           }]/>
  <#assign tags = tags + [{ r"${country}"                                  : "${uiLabelMap.CrmFormTagCountry}"              }]/>
  <#assign tags = tags + [{ r"${mmddyyyy}"                                 : "${uiLabelMap.CrmFormTagDate1}"                }]/>
  <#assign tags = tags + [{ r"${mmddyyyy2}"                                : "${uiLabelMap.CrmFormTagDate2}"                }]/>
  <#assign tags = tags + [{ r"${yyyymmdd}"                                 : "${uiLabelMap.CrmFormTagDate3}"                }]/>
  <#assign tags = tags + [{ r"${yyyymmdd2}"                                : "${uiLabelMap.CrmFormTagDate4}"                }]/>
  <#assign tags = tags + [{ r"${month}"                                    : "${uiLabelMap.CrmFormTagMonthNum}"             }]/>
  <#assign tags = tags + [{ r"${monthName}"                                : "${uiLabelMap.CrmFormTagMonthName}"            }]/>
  <#assign tags = tags + [{ r"${day}"                                      : "${uiLabelMap.CrmFormTagDayNum}"               }]/>
  <#assign tags = tags + [{ r"${year}"                                     : "${uiLabelMap.CrmFormTagYearNum}"              }]/>
  <#assign tags = tags + [{ r"${orderId}"                                  : "${uiLabelMap.CrmFormTagOrderId}"              }]/>
  <#assign tags = tags + [{ r"${externalOrderId}"                          : "${uiLabelMap.CrmFormTagExternalOrderId}"      }]/>
  <#assign tags = tags + [{ r"${orderBillingFirstName}"                    : "${uiLabelMap.CrmFormTagOrderBillFirstName}"   }]/>
  <#assign tags = tags + [{ r"${orderBillingLastName}"                     : "${uiLabelMap.CrmFormTagOrderBillLastName}"    }]/>
  <#assign tags = tags + [{ r"${orderBillingFullName}"                     : "${uiLabelMap.CrmFormTagOrderBillFullName}"    }]/>
  <#assign tags = tags + [{ r"${orderPartyId}"                             : "${uiLabelMap.CrmFormTagOrderPartyId}"         }]/>
  <#assign tags = tags + [{ r"${orderSubtotal}"                            : "${uiLabelMap.CrmFormTagOrderSubtotal}"        }]/>
  <#assign tags = tags + [{ r"${orderTaxTotal}"                            : "${uiLabelMap.CrmFormTagOrderTaxTotal}"        }]/>
  <#assign tags = tags + [{ r"${orderShippingTotal}"                       : "${uiLabelMap.CrmFormTagOrderShipTotal}"       }]/>
  <#assign tags = tags + [{ r"${orderGrandTotal}"                          : "${uiLabelMap.CrmFormTagOrderGrandTotal}"      }]/>
  <#assign tags = tags + [{ r"${orderPaymentTotal}"                        : "${uiLabelMap.CrmFormTagOrderPaymentTotal}"    }]/>
  <#assign tags = tags + [{ r"${orderShippingFirstName}"                   : "${uiLabelMap.CrmFormTagOrderShipFirstName}"   }]/>
  <#assign tags = tags + [{ r"${orderShippingLastName}"                    : "${uiLabelMap.CrmFormTagOrderShipLastName}"    }]/>
  <#assign tags = tags + [{ r"${orderShippingCompanyName}"                 : "${uiLabelMap.CrmFormTagOrderShipCompanyName}" }]/>
  <#assign tags = tags + [{ r"${orderShippingFullName}"                    : "${uiLabelMap.CrmFormTagOrderShipFullName}"    }]/>
  <#assign tags = tags + [{ r"${orderShippingAddress1}"                    : "${uiLabelMap.CrmFormTagOrderShipAddress1}"    }]/>
  <#assign tags = tags + [{ r"${orderShippingAddress2}"                    : "${uiLabelMap.CrmFormTagOrderShipAddress2}"    }]/>
  <#assign tags = tags + [{ r"${orderShippingCity}"                        : "${uiLabelMap.CrmFormTagOrderShipCity}"        }]/>
  <#assign tags = tags + [{ r"${orderShippingStateProvince}"               : "${uiLabelMap.CrmFormTagOrderShipStateProv}"   }]/>
  <#assign tags = tags + [{ r"${orderShippingCountry}"                     : "${uiLabelMap.CrmFormTagOrderShipCountry}"     }]/>
  <#assign tags = tags + [{ r"${orderShippingPostalCode}"                  : "${uiLabelMap.CrmFormTagOrderShipPostalCode}"  }]/>
  <#assign tags = tags + [{ r"${orderShippingPhone}"                       : "${uiLabelMap.CrmFormTagOrderShipPhone}"       }]/>
  <#assign tags = tags + [{ r"${orderDate}"                                : "${uiLabelMap.CrmFormTagOrderDate}"            }]/>
  <#assign tags = tags + [{ r"${shipmentStatus}"                           : "${uiLabelMap.CrmFormTagShipmentStatus}"       }]/>
  <#assign tags = tags + [{ r"${beginList:orderItem}${endList:orderItem}"  : "${uiLabelMap.CrmFormTagOrderItemsList}"       }]/>
  <#assign tags = tags + [{ r"${orderItem.productId}"                      : "${uiLabelMap.CrmFormTagOrderItemProdId}"      }]/>
  <#assign tags = tags + [{ r"${orderItem.productName} "                   : "${uiLabelMap.CrmFormTagOrderItemName}"        }]/>
  <#assign tags = tags + [{ r"${orderItem.internalName}"                   : "${uiLabelMap.CrmFormTagOrderItemIntName}"     }]/>
  <#assign tags = tags + [{ r"${orderItem.quantity}"                       : "${uiLabelMap.CrmFormTagOrderItemQty}"         }]/>

  <#return tags>

</#function>
