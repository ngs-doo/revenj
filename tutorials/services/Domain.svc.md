###Find domain objects by their URIs
       GET /find/{domainObject}/{*uris}
    -> GET /find/{domainObject}?uris={uris}&order={order}
       PUT /find/{domainObject}?order={order}

###Search domain object with specification
       POST /search/{domainObject}/{specification}?limit={limit}&offset={offset}&order={order}&count={count}
    -> PUT  /search/{domainObject}?specification={specification}&limit={limit}&offset={offset}&order={order}&count={count}
       GET  /search/{domainObject}?specification={specification}&limit={limit}&offset={offset}&order={order}&count={count}

###Search domain object using generic specification
    -> PUT /search-generic/{domainObject}?limit={limit}&offset={offset}&order={order}&count={count}
       GET /search-generic/{domainObject}?limit={limit}&offset={offset}&order={order}&count={count}

###Search domain object using expression
    -> PUT /search-expression/{domainObject}?limit={limit}&offset={offset}&order={order}&count={count}

###Count domain object with specification
       POST /count/{domainObject}/{specification}
    -> PUT  /count/{domainObject}?specification={specification}
       GET  /count/{domainObject}?specification={specification}

###Count domain object using generic specification
    -> PUT /count-generic/{domainObject}
       GET /count-generic/{domainObject}

###Count domain object using expression
    -> PUT /count-expression/{domainObject}

###Check if domain object with specification exists
       POST /exists/{domainObject}/{specification}
    -> PUT  /exists/{domainObject}?specification={specification}
       GET  /exists/{domainObject}?specification={specification}

###Check if domain object exists using generic specification
    -> PUT /exists-generic/{domainObject}
       GET /exists-generic/{domainObject}

###Check if domain object exists using expression
    -> PUT /exists-expression/{domainObject}

###Check if domain object with specified URI exists
    -> GET /check/{domainObject}?uri={uri}

###Submit domain event
    -> POST /submit/{domainEvent}?result={result}

###Queue domain event
    -> POST /queue/{domainEvent}

###Submit aggregate domain event
       POST /submit/{aggregate}/{domainEvent}/{*uri}
    -> POST /submit/{aggregate}/{domainEvent}?uri={uri}

###Queue aggregate domain event
       POST /queue/{aggregate}/{domainEvent}/{*uri}
    -> POST /queue/{aggregate}/{domainEvent}?uri={uri}
