INSERT INTO tutorial."DocumentType" 
VALUES ('Passport', 'Passport', true), ('DriverLicense', 'Driver license', true)
ON CONFLICT DO NOTHING;