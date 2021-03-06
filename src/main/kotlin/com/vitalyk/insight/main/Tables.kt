package com.vitalyk.insight.main

// https://scs.fidelity.com/help/mmnet/help_personal_about_tickers.shtml
// Tickers are mostly (up to) 5-character alpha codes, but Shenzhen Stock Exchange,
// for example, has 6-character numerical codes.

// Market Identifier Code (MIC), four alpha character code, ISO 10383:
// https://www.iso20022.org/10383/iso-10383-market-identifier-codes
// https://en.wikipedia.org/wiki/Market_Identifier_Code
// From ISO 10383 FAQ:
// "An operating MIC identifies the entity operating an exchange; it is the ‘parent’ MIC."

// Institution description        |  MIC  |  Operating MIC
// --------------------------------------------------------
// NASDAQ - ALL MARKETS           |  XNAS |  XNAS
// NYSE MKT LLC (AMEX)            |  XASE |  XNYS
// NEW YORK STOCK EXCHANGE, INC.  |  XNYS |  XNYS

// http://www.investopedia.com/terms/m/mic.asp
// https://en.wikipedia.org/wiki/Straight-through_processing

// International Securities Identification Number (12-character alpha-numerical code), ISO 6166:
// https://en.wikipedia.org/wiki/International_Securities_Identification_Number

// TODO: check this out:  https://www.quandl.com/  http://eoddata.com/default.aspx

// Considerations:
// http://wiki.c2.com/?TimeSeriesInSql
// http://jmoiron.net/blog/thoughts-on-timeseries-databases/
// https://news.ycombinator.com/item?id=9805742