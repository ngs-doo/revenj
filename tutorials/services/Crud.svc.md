###Create aggregate root
    -> POST /{root}

###Get domain object with specified URI
       GET /{domainObject}/{*uri}
    -> GET /{domainObject}?uri={uri}

###Change aggregate root
       PUT /{root}/{*uri}
    -> PUT /{root}?uri={uri}

###Delete aggregate root
       DELETE /{root}/{*uri}
    -> DELETE /{root}?uri={uri}

