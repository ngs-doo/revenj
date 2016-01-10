###Insert aggregate root data. Provide data array
    -> POST /persist/{root}
       PUT /persist/{root}

###Get simple olap report with specification
       POST /olap/{cube}/{specification}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}
    -> PUT  /olap/{cube}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}
       GET  /olap/{cube}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}

###Get simple olap report with generic specification
    -> PUT /olap-generic/{cube}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}
       GET /olap-generic/{cube}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}

###Get simple olap report with using expression
    -> PUT /olap-expression/{cube}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}

###Execute service
    -> POST /execute/{service}
