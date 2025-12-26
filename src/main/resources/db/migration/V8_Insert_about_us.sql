INSERT INTO about_section (title, content, is_active)
SELECT 'About Us',
       CONCAT(
        '<p>Welcome to Interest Rates Austria! We provide up-to-date insights on interest rate trends, economic data, and financial guidance to help you make informed decisions.</p>',
        '<p>Our mission is to make complex financial information clear, accessible, and useful for everyone.</p>'
       ),
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM about_section LIMIT 1);
