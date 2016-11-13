module Img
{
    Value Icon
    {
        int code;
        string? description;
        bool[]? bitmask;
        float[] polygon;
    }

    root Bitmap
    {
        Icon primary;
        Icon? secondary;
        Icon[] auxylliary;
        Icon[]? accessory;
    }
}