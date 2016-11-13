module Scraping
{
    root Scrape
    {
        timestamp At;
        string[] Links;
        persistence { optimistic concurrency; }
    }
}