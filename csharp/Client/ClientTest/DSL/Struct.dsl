module Struct
{
    root LinkedList (Number)
    {
        int Number;
        LinkedList? *Next; //pointer to the next item in a list
    }
    snowflake UnrolledList from LinkedList
    {
        Number; //if alias is not declared, name of the used property will be used as alias
        Next.Number FirstNumber; //we can navigate through reference properties
        Next.Next.Number as SecondNumber;

        specification findByNumber 'it => it.Number==number'
        {
            int number;
        }
    }
}