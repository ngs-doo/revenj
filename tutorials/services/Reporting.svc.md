###Populate predefined report
       GET /report/{report}
    -> PUT /report/{report}

###Create predefined report
    -> PUT /report/{report}/{templater}

###Create simple olap report with specification
       POST /olap/{cube}/{templater}/{specification}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}
    -> PUT /olap/{cube}/{templater}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}
       GET /olap/{cube}/{templater}?specification={specification}&dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}

###Create simple olap report with generic specification
    -> PUT /olap-generic/{cube}/{templater}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}
       GET /olap-generic/{cube}/{templater}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}

###Create simple olap report with using expression
    -> PUT /olap-expression/{cube}/{templater}?dimensions={dimensions}&facts={facts}&order={order}&limit={limit}&offset={offset}

###Get aggregate root's history
    -> GET /history/{root}/{*uris}

###Get aggregate root's history
    -> PUT /history/{root}

###Run Templater with specified file for domain object with provided uri
    -> GET /templater/{file}/{domainObject}/{*uri}

###Run Templater with specified file for domain object using provided specification
       POST /templater/{file}/{domainObject}/{specification}
    -> PUT  /templater/{file}/{domainObject}?specification={specification}
       GET  /templater/{file}/{domainObject}?specification={specification}

###Run Templater with specified file for domain object using generic specification
    -> PUT /templater-generic/{file}/{domainObject}
       GET /templater-generic/{file}/{domainObject}

###Run Templater with specified file for domain object using provided expression
    -> PUT /templater-expression/{file}/{domainObject}
